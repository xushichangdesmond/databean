module powerdancer.databean.jackson {
    requires transitive powerdancer.databean;
    requires org.slf4j;
    requires com.fasterxml.jackson.databind;

    exports powerdancer.databean.jackson;

    opens powerdancer.databean.jackson;
}