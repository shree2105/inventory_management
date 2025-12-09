package org.inventory.service;

import org.inventory.model.Stock;

import java.util.List;
import java.util.Optional;

public interface StockService {

    List<Stock> getAllItems(String role, String department) throws Exception;

    Optional<Stock> getById(Integer id) throws Exception;

    Stock insertItem(Stock stock) throws Exception;

    Stock updateItem(Integer id, Stock stock) throws Exception;

    boolean deleteItem(Integer id) throws Exception;

    List<Stock> getAllItems();

}
