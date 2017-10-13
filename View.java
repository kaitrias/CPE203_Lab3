public class View {
    private String product;
    private double price;

    public View(String product, String price){
        this.product = product;
        this.price = Double.parseDouble(price);
    }

    public String getProduct()
    {
        return product;
    }

    public double getPrice()
    {
        return price;
    }
}
