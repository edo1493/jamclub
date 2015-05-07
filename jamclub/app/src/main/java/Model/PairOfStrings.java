package Model;

/**
 * Created by edoardomoreni on 05/05/15.
 */
public class PairOfStrings {

    public String description;
    public String placeId;

    public PairOfStrings(String description, String placeId)
    {
        this.description = description;
        this.placeId = placeId;
    }

    public String getDescription()
    {
        return description;
    }

    public String getPlaceId()
    {
        return placeId;
    }
}
