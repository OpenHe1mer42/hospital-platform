package carely.repository;

public interface IRepository<T> {
    T create(T obj);
    T update(T obj);
    T getById(int id);
    boolean delete(int id);
}
