/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 *
 * @author Hamza
 */
public class Admin extends User{
    public Admin(String userId, String username, String email, String passwordHash, String role) {
        super(userId, username, email, passwordHash, role);
    }
}
