package org.example;

import java.util.function.Predicate;
import java.util.function.Supplier;


public class Main {
     static void main(String[] args) {
         Predicate<Integer> isMajor  = x -> x >= 18;
         Supplier<String> printMessage = () -> "Is your child major ?:  ";

         final Supplier<Integer> age = () -> (int) (Math.random() * 100);

         for (int i = 0; i < 10; i++) {
             System.err.println(isMajorMyChild(age, isMajor, printMessage));
             System.err.println(isMajorMyChild(age, isMajor, printMessage));
         }

     }

     static String isMajorMyChild(final Supplier<Integer> age, Predicate<Integer> majorPredicate,
                                   Supplier<String> message) {
         if (majorPredicate.test(age.get())) {
             return  message.get() + "TRUE";
         } else {
             return  message.get() + "FALSE";
         }
     }


}