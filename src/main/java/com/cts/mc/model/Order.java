package com.cts.mc.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author Bharat Kumar
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

	private String emailId;
	private String orderCode;
	private String name;
	private List<Product> items;
}
