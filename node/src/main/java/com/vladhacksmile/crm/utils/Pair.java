package com.vladhacksmile.crm.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Pair<A, B> {

    private A a;

    private B b;

    public static <A, B> Pair<A, B> make(A a, B b) {
        return new Pair<>(a, b);
    }
}
