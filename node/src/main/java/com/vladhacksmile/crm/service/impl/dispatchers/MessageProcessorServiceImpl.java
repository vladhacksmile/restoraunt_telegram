package com.vladhacksmile.crm.service.impl.dispatchers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vladhacksmile.crm.dao.OrderDAO;
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
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.invoices.SendInvoice;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.payments.OrderInfo;
import org.telegram.telegrambots.meta.api.objects.payments.PreCheckoutQuery;
import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.vladhacksmile.crm.model.result.status.Status.CREATED;

@Service
public class MessageProcessorServiceImpl {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private ProducerServiceImpl producerService;

    @Autowired
    private GPTService gptService;

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
    private OrderDAO orderDAO;

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
            } else if (update.getPreCheckoutQuery() != null) {
                PreCheckoutQuery preCheckoutQuery = update.getPreCheckoutQuery();
                producerService.producerAnswer(processPreCheckoutQuery(preCheckoutQuery));
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (update.getMessage() != null) {
                Message message = update.getMessage();
                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(message.getChatId());
                sendMessage.setText(TelegramEmoji.WARNING + "Ошибка! " + e.getMessage());
                producerService.producerAnswer(sendMessage);
            }
        }
    }

    public AnswerPreCheckoutQuery processPreCheckoutQuery(PreCheckoutQuery preCheckoutQuery) {
        AnswerPreCheckoutQuery answerPreCheckoutQuery = new AnswerPreCheckoutQuery();
        answerPreCheckoutQuery.setOk(true);
        answerPreCheckoutQuery.setPreCheckoutQueryId(preCheckoutQuery.getId());
        return answerPreCheckoutQuery;
    }

    public SendMessage processMessage(Message message) {
        if (message.getSuccessfulPayment() != null) { // Обработка платежа
            TelegramUser telegramUser = telegramUserDAO.findByTelegramId(message.getFrom().getId()).orElse(null);
            if (telegramUser == null) {
                return makeSendMessage(message, TelegramEmoji.WARNING + " Вы не зарегистрированы! Воспользуйтесь командой /reg <почта> <телефон> для регистрации аккаунта!");
            }
            return makeOrder(getAuthUser(telegramUser), message, telegramUser);
        }
        String[] sourceText = message.getText().split(" ");
        String command = sourceText[0];
        if (command.equals("/reg")) {
            return register(message);
        } else {
            TelegramUser telegramUser = telegramUserDAO.findByTelegramId(message.getFrom().getId()).orElse(null);
            if (telegramUser == null) {
                return makeSendMessage(message, TelegramEmoji.WARNING + " Вы не зарегистрированы! Воспользуйтесь командой /reg <почта> <телефон> для регистрации аккаунта!");
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
            if (command.equalsIgnoreCase("/products")) {
                return getProducts(authUser, message, telegramUser);
            }
            if (command.equalsIgnoreCase("/add")) {
                return addCart(authUser, message, telegramUser);
            }
            if (command.equalsIgnoreCase("/remove")) {
                return removeCart(authUser, message, telegramUser);
            }
            if (command.equalsIgnoreCase("/order")) {
                return requestOrder(authUser, message, telegramUser);
            }
            if (command.equalsIgnoreCase("/orders")) {
                return getOrders(authUser, message, telegramUser);
            }
            if (command.equalsIgnoreCase("/status")) {
                return updateOrderStatus(authUser, message, telegramUser);
            }
            if (command.equals("/aicombo")) {
                return makeAICombo(authUser, message, telegramUser);
            }
            if (command.equals("/aibusy")) {
                return makeAIRestaurantBusy(message);
            }
            if (command.equals("/aiadvice")) {
                return adviceAIOrders(authUser, message);
            }
        }
        return makeSendMessage(message, TelegramEmoji.WARNING + " Неизвестная команда!");
    }

    public SendMessage register(Message message) {
        String[] sourceText = message.getText().split(" ");
        Result<TelegramUser> userResult = telegramUserService.findOrSaveUser(message, sourceText[1], sourceText[2]);
        if (userResult.isError()) {
            return makeSendMessage(message, userResult.getStatus() + " " + userResult.getDescription());
        }
        TelegramUser telegramUser = userResult.getObject();
        return makeSendMessage(message, TelegramEmoji.HELLO + " Привет, " + telegramUser.getUserName() + (userResult.getStatus() == CREATED ? ", твой пользователь зарегистрирован!" : "!"));
    }

    public SendMessage makeAIRestaurantBusy(Message message) {
        List<Order> orders = orderDAO.findAll();
        try {
            List<LocalDateTime> orderDates = orders.stream().map(Order::getOrderDate).toList();
            String orderDatesJSON = objectMapper.writeValueAsString(orderDates);
            String gptPrompt = "Анализируй даты заказов клиента представленных в JSON.Спрогнозируй загруженность ресторана почасово, например, 10:00-11:00 - около 10 человек, 11:00-12:00 - около 15 человек.Выводи информацию только в таком виде без рассуждений (текст)." +
                    "Даты заказов клиентов: " + orderDatesJSON;
            String gptResult = gptService.request(gptPrompt);
            if (StringUtils.isEmpty(gptResult)) {
                return makeSendMessage(message, TelegramEmoji.WARNING + "Пустой результат от ИИ!");
            }
            return makeSendMessage(message, gptResult);
        } catch (JsonProcessingException e) {
            return makeSendMessage(message, TelegramEmoji.WARNING + "Ошибка при обработке данных! " + e.getMessage());
        }
    }

    public SendMessage adviceAIOrders(User authUser, Message message) {
        List<Order> orders = orderDAO.findAll();
        Result<SearchResult<ProductDTO>> getProductsResult = productService.getAll(authUser, 1, 100, null, null, null, null, null);
        if (getProductsResult.isError()) {
            return makeErrorSendMessage(message, getProductsResult);
        }
        try {
            List<OrderItem> orderItems = orders.stream().map(Order::getOrderItems).flatMap(List::stream).toList();
            String orderJSON = objectMapper.writeValueAsString(orderItems);
            String productsJSON = objectMapper.writeValueAsString(getProductsResult.getObject().getObjects());
            String gptPrompt = "Анализируй заказы клиентов и товары ресторана в JSON.Выдвини свои предположения и рекомендации, а также возможные прогнозы, учитывая предпочтения клиента о статистике!" +
                    "Все заказы клиентов: " + orderJSON +
                    "Товары ресторана: " + productsJSON +
                    "Предпочтения клиента о статистике: " + message.getText().toLowerCase().replace("/aicombo", "");
            String gptResult = gptService.request(gptPrompt);
            if (StringUtils.isEmpty(gptResult)) {
                return makeSendMessage(message, TelegramEmoji.WARNING + "Пустой результат от ИИ!");
            }
            return makeSendMessage(message, gptResult);
        } catch (JsonProcessingException e) {
            return makeSendMessage(message, TelegramEmoji.WARNING + "Ошибка при обработке данных! " + e.getMessage());
        }
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
            String gptResult = gptService.request(gptPrompt);
            if (StringUtils.isEmpty(gptResult)) {
                return makeSendMessage(message, TelegramEmoji.WARNING + "Пустой результат от ИИ!");
            }
            List<GPTAICombo> combos = objectMapper.readValue(gptResult, new TypeReference<>(){});
            if (CollectionUtils.isEmpty(combos)) {
                return makeSendMessage(message, TelegramEmoji.WARNING + " Не смогли дешифровать!");
            }
            StringBuilder stringBuilder = new StringBuilder(TelegramEmoji.ACCEPT + " Вот, что может предложить тебе ИИ:\n\n");
            int index = 0;
            for (GPTAICombo combo: combos) {
                Result<ProductDTO> getProductResult = productService.getProduct(authUser, combo.getProductId());
                if (getProductResult.isError()) {
                    return makeErrorSendMessage(message, getProductResult);
                }
                ProductDTO productDTO = getProductResult.getObject();
                stringBuilder.append(TelegramEmoji.PIZZA).append(" ").append(++index).append(". ").append(productDTO.getName()).append("\n");
                stringBuilder.append(TelegramEmoji.ID).append(" ID: ").append(productDTO.getId()).append("\n");
                stringBuilder.append(TelegramEmoji.DOLLAR).append(" Цена: ").append(productDTO.getPrice() * combo.getCount()).append("\n");
                stringBuilder.append(TelegramEmoji.GRID).append(" Количество: ").append(combo.getCount()).append("\n");
                stringBuilder.append(TelegramEmoji.INFO).append(" Описание: ").append(productDTO.getDescription()).append("\n");
                stringBuilder.append(TelegramEmoji.PLATE).append(" БЖУ: ").append(productDTO.getProteins()).append("/").append(productDTO.getFats()).append("/").append(productDTO.getNutritional()).append("\n");
                stringBuilder.append(TelegramEmoji.CARROT).append(" Ккал: ").append(productDTO.getCalories()).append("\n");
                stringBuilder.append(TelegramEmoji.WEIGHT).append(" Вес: ").append(productDTO.getWeight()).append("г. \n\n");
            }
            return makeSendMessage(message, stringBuilder.toString());
        } catch (JsonProcessingException e) {
            return makeSendMessage(message, TelegramEmoji.WARNING + "Ошибка при обработке данных! " + e.getMessage());
        }
    }

    public SendMessage getProfile(User authUser, Message message, TelegramUser telegramUser) {
        Result<UserDTO> getUserResult = userService.getUser(authUser, telegramUser.getUserId());
        if (getUserResult.isError()) {
            return makeErrorSendMessage(message, getUserResult);
        }
        UserDTO userDTO = getUserResult.getObject();

        String info = TelegramEmoji.PROFILE + " Информация о твоем аккаунте:\n\n" +
                TelegramEmoji.NAME + " Имя: " + (StringUtils.isNotEmpty(userDTO.getName()) ? userDTO.getName() : "не задано") + "\n" +
                TelegramEmoji.NAME + " Фамилия: " + (StringUtils.isNotEmpty(userDTO.getSurname()) ? userDTO.getSurname() : "не задано") + "\n" +
                TelegramEmoji.NAME + " Отчество: " + (StringUtils.isNotEmpty(userDTO.getPatronymic()) ? userDTO.getPatronymic() : "не задано") + "\n" +
                TelegramEmoji.PHONE + " Телефон: " + (StringUtils.isNotEmpty(userDTO.getPhoneNumber()) ? userDTO.getPhoneNumber() : "не задан") + "\n" +
                TelegramEmoji.EMAIL + " Почта: " + (StringUtils.isNotEmpty(userDTO.getMail()) ? userDTO.getMail() : "не задана");
        return makeSendMessage(message, info);
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

        return makeSendMessage(message, TelegramEmoji.ACCEPT + " Продукты добавлены!");
    }

    public SendMessage addRestaurant(User authUser, Message message, TelegramUser telegramUser) {
        Restaurant restaurant = new Restaurant();
        restaurant.setUserId(authUser.getId());
        restaurant.setName("Ресторан");
        restaurant.setAddress("Санкт-Петербург");
        restaurantDAO.save(restaurant);
        return makeSendMessage(message, TelegramEmoji.ACCEPT + " Ресторан добавлен!");
    }

    public SendMessage updateOrderStatus(User authUser, Message message, TelegramUser telegramUser) {
        String[] args = message.getText().split(" ");
        Result<OrderDTO> updateOrderStatusResult = orderService.updateOrderStatus(authUser, Long.parseLong(args[1]), OrderStatus.valueOf(args[2].toUpperCase()));
        if (updateOrderStatusResult.isError()) {
            return makeErrorSendMessage(message, updateOrderStatusResult);
        }
        return makeSendMessage(message, TelegramEmoji.ACCEPT + " Статус заказа №" + updateOrderStatusResult.getObject().getId() + " обновлен! Новый статус: " + updateOrderStatusResult.getObject().getOrderStatus() + "!");
    }

    public SendMessage requestOrder(User authUser, Message message, TelegramUser telegramUser) {
        Result<ShoppingCartDTO> getUserShoppingCartResult = userService.getUserShoppingCart(authUser, telegramUser.getUserId());
        if (getUserShoppingCartResult.isError()) {
            return makeErrorSendMessage(message, getUserShoppingCartResult);
        }

        long price = getUserShoppingCartResult.getObject().getOrderItems().stream().mapToLong(OrderItem::getPrice).sum();
        LabeledPrice labeledPrice = new LabeledPrice();
        labeledPrice.setAmount((int) (price * 100));
        labeledPrice.setLabel("Оплата заказа");

        SendInvoice sendInvoice = new SendInvoice();
        sendInvoice.setTitle("Оплата заказа");
        sendInvoice.setDescription("В данном окне вы можете оплатить заказ при помощи банковской карты и заполнить необходимую платежную информацию!");
        sendInvoice.setPayload(String.valueOf(telegramUser.getUserId())); // доп инфа, скрыта
        sendInvoice.setProviderToken("381764678:TEST:84874");
        sendInvoice.setMaxTipAmount(1);
        sendInvoice.setSuggestedTipAmounts(List.of(1));
        sendInvoice.setChatId(message.getChatId());
        sendInvoice.setPrices(Collections.singletonList(labeledPrice));
        sendInvoice.setCurrency("RUB");
        producerService.producerAnswer(sendInvoice);
        return makeSendMessage(message, TelegramEmoji.ACCEPT + " Ваш заказ сформирован и ожидает оплаты! Сумма заказа: " + price + " руб! Оплатите его при помощи платежного счета Telegram!");
    }

    public SendMessage makeOrder(User authUser, Message message, TelegramUser telegramUser) {
        SuccessfulPayment successfulPayment = message.getSuccessfulPayment();
//        OrderInfo orderInfo = successfulPayment.getOrderInfo();
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setUserId(telegramUser.getUserId());
        orderDTO.setRestaurantId(1L);
        orderDTO.setTotalAmount(successfulPayment.getTotalAmount());
        orderDTO.setShippingOptionId(successfulPayment.getShippingOptionId());
        orderDTO.setTelegramPaymentChargeId(successfulPayment.getTelegramPaymentChargeId());
        orderDTO.setProviderPaymentChargeId(successfulPayment.getProviderPaymentChargeId());
        Result<OrderDTO> createOrderResult = orderService.createOrder(authUser, orderDTO);
        if (createOrderResult.isError()) {
            return makeErrorSendMessage(message, createOrderResult);
        }

        long price = createOrderResult.getObject().getOrderItems().stream().mapToLong(OrderItem::getPrice).sum();
        return makeSendMessage(message, TelegramEmoji.ACCEPT + " Ваш заказ №" + createOrderResult.getObject().getId() + " оформлен и принят в работу! Сумма заказа: " + price + " руб!");
    }

    public SendMessage getProducts(User authUser, Message message, TelegramUser telegramUser) {
        Result<SearchResult<ProductDTO>> getProductsResult = productService.getAll(authUser, 1, 10, null, null, null, null, null);
        if (getProductsResult.isError()) {
            return makeErrorSendMessage(message, getProductsResult);
        }
        StringBuilder stringBuilder = new StringBuilder(TelegramEmoji.BURGER.toString());
        stringBuilder.append(" Список продуктов:\n\n");
        int index = 0;
        for (ProductDTO productDTO: getProductsResult.getObject().getObjects()) {
            stringBuilder.append(TelegramEmoji.PIZZA).append(" ").append(++index).append(". ").append(productDTO.getName()).append("\n");
            stringBuilder.append(TelegramEmoji.ID).append(" ID: ").append(productDTO.getId()).append("\n");
            stringBuilder.append(TelegramEmoji.DOLLAR).append(" Цена: ").append(productDTO.getPrice()).append("\n");
            stringBuilder.append(TelegramEmoji.GRID).append(" Количество: ").append(productDTO.getCount()).append("\n");
            stringBuilder.append(TelegramEmoji.INFO).append(" Описание: ").append(productDTO.getDescription()).append("\n");
            stringBuilder.append(TelegramEmoji.PLATE).append(" БЖУ: ").append(productDTO.getProteins()).append("/").append(productDTO.getFats()).append("/").append(productDTO.getNutritional()).append("\n");
            stringBuilder.append(TelegramEmoji.CARROT).append(" Ккал: ").append(productDTO.getCalories()).append("\n");
            stringBuilder.append(TelegramEmoji.WEIGHT).append(" Вес: ").append(productDTO.getWeight()).append("г. \n\n");
        }
        return makeSendMessage(message, stringBuilder.toString());
    }

    public SendMessage getOrders(User authUser, Message message, TelegramUser telegramUser) {
        Result<SearchResult<OrderDTO>> getAllOrdersByUserResult = orderService.getAllOrdersByUser(authUser, 1, 10, telegramUser.getUserId());
        if (getAllOrdersByUserResult.isError()) {
            return makeErrorSendMessage(message, getAllOrdersByUserResult);
        }
        StringBuilder stringBuilder = new StringBuilder(TelegramEmoji.SHOP_BAG.toString());
        stringBuilder.append(" Список ваших заказов:\n\n");
        for (OrderDTO orderDTO: getAllOrdersByUserResult.getObject().getObjects()) {
            int index = 0;
            int price = 0;
            stringBuilder.append("-----------------------------\n");
            stringBuilder.append(TelegramEmoji.SHOP).append(" Заказ №: ").append(orderDTO.getId()).append("\n");
            stringBuilder.append(TelegramEmoji.INFO).append(" Статус: ").append(orderDTO.getOrderStatus().name()).append("\n");
            stringBuilder.append(TelegramEmoji.CALENDAR).append(" Дата: ").append(orderDTO.getOrderDate().format(formatter)).append("\n\n");
            stringBuilder.append(TelegramEmoji.BURGER).append(" Товары:\n\n");
            for (OrderItem orderItem : orderDTO.getOrderItems()) {
                Result<ProductDTO> getProductResult = productService.getProduct(authUser, orderItem.getProductId());
                if (getProductResult.isError()) {
                    return makeErrorSendMessage(message, getProductResult);
                }
                ProductDTO productDTO = getProductResult.getObject();
                stringBuilder.append(TelegramEmoji.PIZZA).append(" ").append(++index).append(". ").append(productDTO.getName()).append("\n");
                stringBuilder.append(TelegramEmoji.DOLLAR).append(" Цена: ").append(productDTO.getPrice() * productDTO.getCount()).append("\n");
                stringBuilder.append(TelegramEmoji.GRID).append(" Количество: ").append(orderItem.getCount()).append("\n\n");
                price += productDTO.getPrice() * orderItem.getCount();
            }
            stringBuilder.append(TelegramEmoji.EURO).append(" Общая цена: ").append(price).append("\n\n");
        }
        return makeSendMessage(message, stringBuilder.toString());
    }

    public SendMessage addCart(User authUser, Message message, TelegramUser telegramUser) {
        Result<ShoppingCartDTO> getUserShoppingCartResult = userService.getUserShoppingCart(authUser, telegramUser.getUserId());
        if (getUserShoppingCartResult.isError()) {
            return makeErrorSendMessage(message, getUserShoppingCartResult);
        }
        ShoppingCartDTO shoppingCartDTO = getUserShoppingCartResult.getObject();
        List<OrderItem> orderItems = CollectionUtils.isEmpty(shoppingCartDTO.getOrderItems()) ? new ArrayList<>() : shoppingCartDTO.getOrderItems();
        Long productId = Long.parseLong(message.getText().split(" ")[1]);
        Result<ProductDTO> getProductResult = productService.getProduct(authUser, productId);
        if (getProductResult.isError()) {
            return makeErrorSendMessage(message, getProductResult);
        }
        Map<Long, OrderItem> orderItemMap = orderItems.stream().collect(Collectors.toMap(OrderItem::getProductId, o -> o));
        ProductDTO productDTO = getProductResult.getObject();
        OrderItem orderItem = orderItemMap.get(productId);
        if (orderItem == null) {
            orderItem = new OrderItem();
            orderItem.setCount(Integer.parseInt(message.getText().split(" ")[2]));
            orderItem.setProductId(productId);
            orderItem.setPrice((long) productDTO.getPrice() * orderItem.getCount());
        } else {
            orderItem.setCount(orderItem.getCount() + 1);
            orderItem.setPrice((long) productDTO.getPrice() * orderItem.getCount());
        }
        orderItemMap.put(productId, orderItem);
        shoppingCartDTO.setOrderItems(orderItemMap.values().stream().toList());
        Result<ShoppingCartDTO> updateUserShoppingCartResult = userService.updateUserShoppingCart(authUser, shoppingCartDTO);
        if (updateUserShoppingCartResult.isError()) {
            return makeErrorSendMessage(message, updateUserShoppingCartResult);
        }
        return makeSendMessage(message, TelegramEmoji.ACCEPT + " Товар " + productDTO.getName() + " добавлен в корзину! Количество " + orderItem.getCount() + "!");
    }

    public SendMessage removeCart(User authUser, Message message, TelegramUser telegramUser) {
        Result<ShoppingCartDTO> getUserShoppingCartResult = userService.getUserShoppingCart(authUser, telegramUser.getUserId());
        if (getUserShoppingCartResult.isError()) {
            return makeErrorSendMessage(message, getUserShoppingCartResult);
        }
        ShoppingCartDTO shoppingCartDTO = getUserShoppingCartResult.getObject();
        List<OrderItem> orderItems = CollectionUtils.isEmpty(shoppingCartDTO.getOrderItems()) ? new ArrayList<>() : shoppingCartDTO.getOrderItems();
        Long productId = Long.parseLong(message.getText().split(" ")[1]);
        int rm = Integer.parseInt(message.getText().split(" ")[2]);
        Result<ProductDTO> getProductResult = productService.getProduct(authUser, productId);
        if (getProductResult.isError()) {
            return makeErrorSendMessage(message, getProductResult);
        }
        Map<Long, OrderItem> orderItemMap = orderItems.stream().collect(Collectors.toMap(OrderItem::getProductId, o -> o));
        ProductDTO productDTO = getProductResult.getObject();
        OrderItem orderItem = orderItemMap.get(productId);
        if (orderItem == null) {
            return makeSendMessage(message, TelegramEmoji.WARNING + " Товар не найден в корзине!");
        } else {
            if (rm > orderItem.getCount()) {
                return makeSendMessage(message, TelegramEmoji.WARNING + " Некорректное количество!");
            }
            orderItem.setCount(orderItem.getCount() - (Integer.parseInt(message.getText().split(" ")[2])));
            orderItem.setPrice((long) productDTO.getPrice() * orderItem.getCount());
        }
        if (orderItem.getCount() > 0) {
            orderItemMap.put(productId, orderItem);
        } else {
            orderItemMap.remove(productId);
        }
        shoppingCartDTO.setOrderItems(orderItemMap.values().stream().toList());
        Result<ShoppingCartDTO> updateUserShoppingCartResult = userService.updateUserShoppingCart(authUser, shoppingCartDTO);
        if (updateUserShoppingCartResult.isError()) {
            return makeErrorSendMessage(message, updateUserShoppingCartResult);
        }
        return makeSendMessage(message, TelegramEmoji.ACCEPT + " Товар " + productDTO.getName() + " удален из корзины! Текущее количество " + orderItem.getCount() + "!");
    }

    private SendMessage formatCart(User authUser, Message message, List<OrderItem> orderItems) {
        if (CollectionUtils.isEmpty(orderItems)) {
            return makeSendMessage(message, TelegramEmoji.SHOP_BAG + " Ваша корзина пуста!");
        }

        StringBuilder stringBuilder = new StringBuilder(TelegramEmoji.SHOP_BAG.toString());
        stringBuilder.append(" Ваша корзина\n\n");
        int index = 0;
        int price = 0;
        for (OrderItem orderItem: orderItems) {
            Result<ProductDTO> getProductResult = productService.getProduct(authUser, orderItem.getProductId());
            if (getProductResult.isError()) {
                return makeErrorSendMessage(message, getProductResult);
            }
            ProductDTO productDTO = getProductResult.getObject();
            stringBuilder.append(TelegramEmoji.PIZZA).append(" ").append(++index).append(". ").append(productDTO.getName()).append("\n");
            stringBuilder.append(TelegramEmoji.ID).append(" ID: ").append(productDTO.getId()).append("\n");
            stringBuilder.append(TelegramEmoji.DOLLAR).append(" Цена: ").append(productDTO.getPrice() * productDTO.getCount()).append("\n");
            stringBuilder.append(TelegramEmoji.GRID).append(" Количество: ").append(orderItem.getCount()).append("\n\n");
            price += productDTO.getPrice() * orderItem.getCount();
        }
        stringBuilder.append(TelegramEmoji.EURO).append(" Общая цена: ").append(price).append("\n");
        return makeSendMessage(message, stringBuilder.toString());
    }

    public SendMessage getCart(User authUser, Message message, TelegramUser telegramUser) {
        Result<ShoppingCartDTO> getUserShoppingCartResult = userService.getUserShoppingCart(authUser, telegramUser.getUserId());
        if (getUserShoppingCartResult.isError()) {
            return makeErrorSendMessage(message, getUserShoppingCartResult);
        }
        return formatCart(authUser, message, getUserShoppingCartResult.getObject().getOrderItems());
    }

    public User getAuthUser(TelegramUser telegramUser) {
        return userDAO.findById(telegramUser.getUserId()).orElse(null);
    }

    public static SendMessage makeErrorSendMessage(Message message, Result<?> answer) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(TelegramEmoji.WARNING + " Произошла ошибка! Статус: " + answer.getStatus() + ", описание: " + answer.getDescription() + "!");
        return sendMessage;
    }

    public static SendMessage makeSendMessage(Message message, String answer) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(answer);
        return sendMessage;
    }
}
