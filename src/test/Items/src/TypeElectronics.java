public class TypeElectronics implements ItemsFactory{
    @Override
    public Items createItems(ItemsAttributes request) {
        return new Electronics(request.getOwner(), request.getStartingPrice(), request.getDescription(), request.getWarranty(), request.getBrand());
    }
}
