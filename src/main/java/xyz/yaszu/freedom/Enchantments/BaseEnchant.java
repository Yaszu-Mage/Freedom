package xyz.yaszu.freedom.Enchantments;

public interface BaseEnchant {
    public void maxLevel();
    //max level of enchantment
    public void effect();
    //basically enchantment name we are checking for
    public String name();
    //Silly silly
    //sILLY BILLY
    public int anvilCost();
    public int maxEnchantmentLevel();
    public int minEnchantmentLevel();
    public int minEnchantCost();
    public int maxEnchantCost();
    //How would I register enchants mid match????
    //it's lwk weird
}
