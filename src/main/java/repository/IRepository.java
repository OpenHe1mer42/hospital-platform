package repository;

import service.DatabaseService;

import java.sql.Connection;

public interface IRepository<T> {
    Connection connection = DatabaseService.getConnection();
    T create(T obj);
    T update(T obj);
    T getById(int id);
    boolean delete(int id);
}
