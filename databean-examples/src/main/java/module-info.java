module powerdancer.databean.examples {
    requires powerdancer.databean;

    // only for the jackson examples
    requires powerdancer.databean.jackson;
    requires com.fasterxml.jackson.databind;

    opens powerdancer.databean.examples;
    opens powerdancer.databean.jackson.examples;
}