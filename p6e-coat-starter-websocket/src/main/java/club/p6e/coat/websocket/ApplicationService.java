package club.p6e.coat.websocket;

/**
 * @author lidashuang
 * @version 1.0
 */
public interface ApplicationService<T extends UserDataModel> {

    public String award(String token);

    public SessionChannel<T> validate(T data);

}
