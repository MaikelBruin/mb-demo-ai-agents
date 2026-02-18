package mb.demo.applications.ai.agents.routes;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RouteBuilderConstants {
    public static final String DIRECT_ROUTE_GET_HAS_AVAILABLE_RATS = "direct:getHasAvailableRats";
    public static final String DIRECT_ROUTE_GET_AVAILABILITY_RATIO = "direct:getPetAvailabilityRatio";
    public static final String DIRECT_ROUTE_GET_TOTAL_DOGS = "direct:getTotalNumberOfDogs";
    public static final String DIRECT_ROUTE_GET_TOTAL_CATS = "direct:getTotalNumberOfCats";
    public static final String DIRECT_ROUTE_GET_TOTAL_AVAILABLE = "direct:getTotalNumberOfAvailablePets";
    public static final String DIRECT_ROUTE_FIND_TOTAL = "direct:findTotalNumberOfPetsByTag";

}
