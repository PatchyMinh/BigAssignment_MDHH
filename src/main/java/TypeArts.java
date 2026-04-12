public class TypeArts implements ItemsFactory{
    @Override
    public Items createItems(ItemsAttributes request) {
        return new Arts(request.getOwner(), request.getStartingPrice(), request.getDescription(), request.getArtistName(), request.getReleaseDate());
    }
}
