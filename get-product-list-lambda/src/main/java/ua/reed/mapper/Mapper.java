package ua.reed.mapper;

public interface Mapper<S, T> {

    T fromSource(S source);

    S fromTarget(T target);

}
