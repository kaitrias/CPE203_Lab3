public class Buy {
    private String product;
    private double price;
    private String quantity;

    public Buy(String product, String price, String quantity){
        this.product = product;
        this.price = Double.parseDouble(price);
        this.quantity = quantity;
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
