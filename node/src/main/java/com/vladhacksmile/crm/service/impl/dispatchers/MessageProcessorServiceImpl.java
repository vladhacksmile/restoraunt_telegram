package com.vladhacksmile.crm.service.impl.dispatchers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vladhacksmile.crm.dao.RestaurantDAO;
import com.vladhacksmile.crm.dao.TelegramUserDAO;
import com.vladhacksmile.crm.dao.UserDAO;
import com.vladhacksmile.crm.dto.OrderDTO;
import com.vladhacksmile.crm.dto.ProductDTO;
import com.vladhacksmile.crm.dto.ShoppingCartDTO;
import com.vladhacksmile.crm.dto.auth.UserDTO;
import com.vladhacksmile.crm.gpt.GPTAICombo;
import com.vladhacksmile.crm.gpt.GPTService;
import com.vladhacksmile.crm.gpt.TelegramEmoji;
import com.vladhacksmile.crm.jdbc.*;
import com.vladhacksmile.crm.model.result.Result;
import com.vladhacksmile.crm.model.result.SearchResult;
import com.vladhacksmile.crm.service.OrderService;
import com.vladhacksmile.crm.service.ProductService;
import com.vladhacksmile.crm.service.UserService;
import com.vladhacksmile.crm.service.impl.TelegramUserServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MessageProcessorServiceImpl {

    @Autowired
    private ProducerServiceImpl producerService;

    @Autowired
    private GPTService gptTest;

    @Autowired
    private TelegramUserServiceImpl telegramUserService;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private TelegramUserDAO telegramUserDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RestaurantDAO restaurantDAO;

    private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public void processTextMessage(Update update) {
        try {
            if (update.getMessage() != null) {
                Message message = update.getMessage();
                producerService.producerAnswer(processMessage(message));
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (update.getMessage() != null) {
                Message message = update.getMessage();
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId());
                sendMessage.setText("Ошибка! " + e.getMessage());
                producerService.producerAnswer(sendMessage);
            }
        }
    }

    public SendMessage processMessage(Message message) {
        String[] sourceText = message.getText().split(" ");
        List<String> resultText = new ArrayList<>();
        String command = sourceText[0];
        if (command.equals("/reg")) {
            Result<TelegramUser> userResult = telegramUserService.findOrSaveUser(message, sourceText[1], sourceText[2]);
            if (userResult.isError()) {
                return makeSendMessage(message, userResult.getStatus() + " " + userResult.getDescription());
            }
            return makeSendMessage(message, "Вы зарегистрированы!");
        } else {
            TelegramUser telegramUser = telegramUserDAO.findByTelegramId(message.getFrom().getId()).orElse(null);
            if (telegramUser == null) {
                return makeSendMessage(message, "Вы не зарегистрированы! Воспользуйтесь командой /reg <mail> <phone> для регистрации аккаунта!");
            }
            User authUser = getAuthUser(telegramUser);
            if (command.equalsIgnoreCase("/okey")) {
                addRestaurant(authUser, message, telegramUser);
                return addProduct(authUser, message, telegramUser);
            }
            if (command.equalsIgnoreCase("/profile")) {
                return getProfile(authUser, message, telegramUser);
            }
            if (command.equalsIgnoreCase("/cart")) {
                return getCart(authUser, message, telegramUser);
            }
            if (command.equalsIgnoreCase("/rest")) {
                return addRestaurant(authUser, message, telegramUser);
            }
            if (command.equalsIgnoreCase("/add_products")) {
                return addProduct(authUser, message, telegramUser);
            }
            if (command.equalsIgnoreCase("/product")) {
                return getProducts(authUser, message, telegramUser);
            }
            if (command.equalsIgnoreCase("/add_cart")) {
                return addCart(authUser, message, telegramUser);
            }
            if (command.equalsIgnoreCase("/order")) {
                return makeOrder(authUser, message, telegramUser);
            }
            if (command.equalsIgnoreCase("/orders")) {
                return getOrders(authUser, message, telegramUser);
            }
            if (command.equals("/aicombo")) {
                return makeAICombo(authUser, message, telegramUser);
            }
        }
//        String result = chatClient.call(message.getText());
        resultText.add("Неизвестный запрос!");
//        resultText.add(gptTest.request(message.getText()));
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(formatToString(resultText));
        return sendMessage;
    }

    public SendMessage makeAICombo(User authUser, Message message, TelegramUser telegramUser) {
        Result<SearchResult<OrderDTO>> getAllOrdersByUserResult = orderService.getAllOrdersByUser(authUser, 1, 10, telegramUser.getUserId());
        if (getAllOrdersByUserResult.isError()) {
            return makeErrorSendMessage(message, getAllOrdersByUserResult);
        }
        Result<SearchResult<ProductDTO>> getProductsResult = productService.getAll(authUser, 1, 5, null, null, null, null, null);
        if (getProductsResult.isError()) {
            return makeErrorSendMessage(message, getProductsResult);
        }
        try {
            List<OrderItem> orderItems = getAllOrdersByUserResult.getObject().getObjects().stream().map(OrderDTO::getOrderItems).flatMap(List::stream).toList();
            String orderJSON = objectMapper.writeValueAsString(orderItems);
            String productsJSON = objectMapper.writeValueAsString(getProductsResult.getObject().getObjects());
            String gptPrompt = "Анализируй заказы клиента в JSON.Предложи комбо из горячего блюда и напитка.Учти предпочтения клиента и добавь новые.Ответ в JSON и с существующими productId!Минимум 2 товара, максимум 3!Формат:" +
                    "[{\"productId\": (PRODUCT_ID_1), \"count\": (кол-во товара), {\"productId\": {PRODUCT_ID_1}, \"count\": {кол-во товара}}]. Если нет или ошибка, то []!" +
            "Заказы клиента: " + orderJSON +
            "Товары ресторана: " + productsJSON +
            "Предпочтения клиента: " + message.getText().toLowerCase().replace("/aicombo", "");
            String gptResult = gptTest.request(gptPrompt);
            if (StringUtils.isEmpty(gptResult)) {
                return makeSendMessage(message, "Что-то с GPT!");
            }
            List<GPTAICombo> gptaiCombos = objectMapper.readValue(gptResult, new TypeReference<>(){});
            if (CollectionUtils.isEmpty(gptaiCombos)) {
                return makeSendMessage(message, "Не смогли дешифровать!");
            }
            StringBuilder aiResult = new StringBuilder("Вот, что может предложить тебе ИИ:\n\n");
            int ind = 0;
            for (GPTAICombo gptaiCombo: gptaiCombos) {
                Result<ProductDTO> getProductResult = productService.getProduct(authUser, gptaiCombo.getProductId());
                if (getProductResult.isError()) {
                    return makeErrorSendMessage(message, getProductResult);
                }
                ProductDTO productDTO = getProductResult.getObject();
                aiResult.append(TelegramEmoji.PIZZA).append(" ").append(++ind).append(". ").append(productDTO.getName()).append("\n");
                aiResult.append(TelegramEmoji.ID).append(" ID: ").append(productDTO.getId()).append("\n");
                aiResult.append(TelegramEmoji.DOLLAR).append(" Цена: ").append(productDTO.getPrice()).append("\n");
                aiResult.append(TelegramEmoji.GRID).append(" Количество: ").append(productDTO.getCount()).append("\n");
                aiResult.append(TelegramEmoji.INFO).append(" Описание: ").append(productDTO.getDescription()).append("\n");
                aiResult.append(TelegramEmoji.PLATE).append(" БЖУ: ").append(productDTO.getProteins()).append("/").append(productDTO.getFats()).append("/").append(productDTO.getNutritional()).append("\n");
                aiResult.append(TelegramEmoji.CARROT).append(" Ккал: ").append(productDTO.getCalories()).append("\n");
                aiResult.append(TelegramEmoji.WEIGHT).append(" Вес: ").append(productDTO.getWeight()).append("г. \n\n");
            }
            return makeSendMessage(message, aiResult.toString());
        } catch (JsonProcessingException e) {
            return makeSendMessage(message, "Ошибка при парсинге! " + e.getMessage());
        }
    }

    public SendMessage getProfile(User authUser, Message message, TelegramUser telegramUser) {
        Result<UserDTO> getUserResult = userService.getUser(authUser, telegramUser.getUserId());
        if (getUserResult.isError()) {
            return makeErrorSendMessage(message, getUserResult);
        }

        return makeSendMessage(message, getUserResult.getObject().toString());
    }

    public SendMessage addProduct(User authUser, Message message, TelegramUser telegramUser) {
        List<ProductDTO> productDTOS = new ArrayList<>();
        // Создаем объект пиццы
        ProductDTO pizza = new ProductDTO();
        pizza.setCount(1L);
        pizza.setName("Пицца Карбонара");
        pizza.setDescription("Аппетитная пицца с беконом, яйцом, сыром и соусом карбонара");
        pizza.setPictureId("pizza_carbonara_img");
        pizza.setPrice(700);
        pizza.setWeight(500);
        pizza.setCalories(1200);
        pizza.setNutritional(60);
        pizza.setProteins(30);
        pizza.setFats(20);

        // Создаем объект салата
        ProductDTO salad = new ProductDTO();
        salad.setCount(1L);
        salad.setName("Цезарь с курицей");
        salad.setDescription("Салат с кусочками куриного мяса, сыром пармезан, листьями салата и соусом Цезарь");
        salad.setPictureId("caesar_salad_img");
        salad.setPrice(450);
        salad.setWeight(300);
        salad.setCalories(400);
        salad.setNutritional(25);
        salad.setProteins(15);
        salad.setFats(20);

        // Создаем объект напитка
        ProductDTO drink = new ProductDTO();
        drink.setCount(1L);
        drink.setName("Кола");
        drink.setDescription("Охлажденный газированный напиток");
        drink.setPictureId("cola_img");
        drink.setPrice(100);
        drink.setWeight(330);
        drink.setCalories(150);
        drink.setNutritional(0);
        drink.setProteins(0);
        drink.setFats(0);

        productDTOS.add(pizza);
        productDTOS.add(salad);
        productDTOS.add(drink);

        // Создаем объект пиццы
        ProductDTO hawaiianPizza = new ProductDTO();
        hawaiianPizza.setCount(1L);
        hawaiianPizza.setName("Гавайская пицца");
        hawaiianPizza.setDescription("Сочная пицца с ветчиной, ананасами, моцареллой и томатным соусом");
        hawaiianPizza.setPictureId("hawaiian_pizza_img");
        hawaiianPizza.setPrice(650);
        hawaiianPizza.setWeight(480);
        hawaiianPizza.setCalories(1100);
        hawaiianPizza.setNutritional(55);
        hawaiianPizza.setProteins(28);
        hawaiianPizza.setFats(18);

        // Создаем объект гамбургера
        ProductDTO cheeseburger = new ProductDTO();
        cheeseburger.setCount(1L);
        cheeseburger.setName("Чизбургер");
        cheeseburger.setDescription("Сочная говяжья котлета, сыр чеддер, свежие овощи и соус в пышной булочке");
        cheeseburger.setPictureId("cheeseburger_img");
        cheeseburger.setPrice(550);
        cheeseburger.setWeight(250);
        cheeseburger.setCalories(700);
        cheeseburger.setNutritional(40);
        cheeseburger.setProteins(25);
        cheeseburger.setFats(15);

        // Создаем объект сока
        ProductDTO orangeJuice = new ProductDTO();
        orangeJuice.setCount(1L);
        orangeJuice.setName("Апельсиновый сок");
        orangeJuice.setDescription("Свежевыжатый сок из спелых апельсинов без добавления сахара и консервантов");
        orangeJuice.setPictureId("orange_juice_img");
        orangeJuice.setPrice(150);
        orangeJuice.setWeight(300);
        orangeJuice.setCalories(120);
        orangeJuice.setNutritional(2);
        orangeJuice.setProteins(1);
        orangeJuice.setFats(0);

        // Добавляем продукты в список
        productDTOS.add(hawaiianPizza);
        productDTOS.add(cheeseburger);
        productDTOS.add(orangeJuice);

        for (ProductDTO productDTO: productDTOS) {
            Result<ProductDTO> createProductResult = productService.createProduct(authUser, productDTO);
            if (createProductResult.isError()) {
                return makeErrorSendMessage(message, createProductResult);
            }
        }

        return makeSendMessage(message, "Продукты добавлены!");
    }

    public SendMessage addRestaurant(User authUser, Message message, TelegramUser telegramUser) {
        Restaurant restaurant = new Restaurant();
        restaurant.setUserId(authUser.getId());
        restaurant.setName("Ресторан");
        restaurant.setAddress("Санкт-Петербург");
        restaurantDAO.save(restaurant);
        return makeSendMessage(message, "Ресторан добавлен!");
    }

    public SendMessage makeOrder(User authUser, Message message, TelegramUser telegramUser) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setUserId(telegramUser.getUserId());
        orderDTO.setRestaurantId(1L);
        Result<OrderDTO> createOrderResult = orderService.createOrder(authUser, orderDTO);
        if (createOrderResult.isError()) {
            return makeErrorSendMessage(message, createOrderResult);
        }
        return makeSendMessage(message, createOrderResult.getObject().toString());
    }

    public SendMessage getProducts(User authUser, Message message, TelegramUser telegramUser) {
        Result<SearchResult<ProductDTO>> getProductsResult = productService.getAll(authUser, 1, 5, null, null, null, null, null);
        if (getProductsResult.isError()) {
            return makeErrorSendMessage(message, getProductsResult);
        }

        return makeSendMessage(message, getProductsResult.getObject().toString());
    }

    public SendMessage getOrders(User authUser, Message message, TelegramUser telegramUser) {
        Result<SearchResult<OrderDTO>> getAllOrdersByUserResult = orderService.getAllOrdersByUser(authUser, 1, 10, telegramUser.getUserId());
        if (getAllOrdersByUserResult.isError()) {
            return makeErrorSendMessage(message, getAllOrdersByUserResult);
        }

        return makeSendMessage(message, getAllOrdersByUserResult.getObject().toString());
    }

    public SendMessage addCart(User authUser, Message message, TelegramUser telegramUser) {
        Result<ShoppingCartDTO> getUserShoppingCartResult = userService.getUserShoppingCart(authUser, telegramUser.getUserId());
        if (getUserShoppingCartResult.isError()) {
            return makeErrorSendMessage(message, getUserShoppingCartResult);
        }
        ShoppingCartDTO shoppingCartDTO = getUserShoppingCartResult.getObject();
        List<OrderItem> orderItems = CollectionUtils.isEmpty(shoppingCartDTO.getOrderItems()) ? new ArrayList<>(): shoppingCartDTO.getOrderItems();
        Long productId = Long.parseLong(message.getText().split(" ")[1]);
        Result<ProductDTO> getProductResult = productService.getProduct(authUser, productId);
        if (getProductResult.isError()) {
            return makeErrorSendMessage(message, getProductResult);
        }
        Map<Long, OrderItem> orderItemMap = orderItems.stream().collect(Collectors.toMap(OrderItem::getProductId, o -> o));
        OrderItem orderItem = orderItemMap.get(productId);
        if (orderItem == null) {
            orderItem = new OrderItem();
            orderItem.setCount(Integer.parseInt(message.getText().split(" ")[2]));
            orderItem.setProductId(productId);
            orderItem.setPrice((long) getProductResult.getObject().getPrice() * orderItem.getCount());
        } else {
            orderItem.setCount(orderItem.getCount() + 1);
            orderItem.setPrice((long) getProductResult.getObject().getPrice() * orderItem.getCount());
        }
        orderItemMap.put(productId, orderItem);
        shoppingCartDTO.setOrderItems(orderItemMap.values().stream().toList());
        Result<ShoppingCartDTO> updateUserShoppingCartResult = userService.updateUserShoppingCart(authUser, shoppingCartDTO);
        if (updateUserShoppingCartResult.isError()) {
            return makeErrorSendMessage(message, updateUserShoppingCartResult);
        }
        return makeSendMessage(message, updateUserShoppingCartResult.getObject().toString());
    }

    public SendMessage getCart(User authUser, Message message, TelegramUser telegramUser) {
        Result<ShoppingCartDTO> getUserShoppingCartResult = userService.getUserShoppingCart(authUser, telegramUser.getUserId());
        if (getUserShoppingCartResult.isError()) {
            return makeErrorSendMessage(message, getUserShoppingCartResult);
        }
        ShoppingCartDTO shoppingCartDTO = getUserShoppingCartResult.getObject();
        if (CollectionUtils.isEmpty(shoppingCartDTO.getOrderItems())) {
            return makeSendMessage(message, "Ваша корзина пуста!");
        }

        return makeSendMessage(message, getUserShoppingCartResult.getObject().toString());
    }

    public User getAuthUser(TelegramUser telegramUser) {
        return userDAO.findById(telegramUser.getUserId()).orElse(null);
    }

    public static SendMessage makeErrorSendMessage(Message message, Result<?> answer) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText("Произошла ошибка! Статус " + answer.getStatus() + ", описание " + answer.getDescription() + "!");
        return sendMessage;
    }

    public static SendMessage makeSendMessage(Message message, String answer) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(answer);
        return sendMessage;
    }

    public String formatToString(Collection<String> strings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (String s: strings) {
            stringBuilder.append(s);
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }
}
