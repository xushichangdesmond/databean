module powerdancer.databean {
    requires transitive powerdancer.asmproxy;
    requires org.slf4j;

    exports powerdancer.databean;

    opens powerdancer.databean;
    opens powerdancer.databean.internal;

    exports powerdancer.databean.internal to powerdancer.databean.jackson;
}