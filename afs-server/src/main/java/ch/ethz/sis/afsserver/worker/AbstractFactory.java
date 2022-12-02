package ch.ethz.sis.afsserver.worker;

public abstract class AbstractFactory<INITIALIZER, CREATOR, CREATED> {
    public abstract void init(INITIALIZER initParameter) throws Exception;
    public abstract CREATED create(CREATOR createParameter) throws Exception;
}
