package powerdancer.databean;

/**
 * extend this interface from your databean builder interface if you wish for it to provide a build() method
 * @param <T>
 */
public interface DataBeanBuilder<T> {
    T build();
}