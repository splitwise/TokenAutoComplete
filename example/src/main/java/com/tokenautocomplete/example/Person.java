package com.tokenautocomplete.example;

import java.io.Serializable;

/**
 * Simple container object for contact data
 *
 * Created by mgod on 9/12/13.
 * @author mgod
 */
public class Person implements Serializable{
    private String name;
    private String email;

    public Person(String n, String e) {
        name = n;
        email = e;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }

    @Override
    public String toString() { return name; }
}
