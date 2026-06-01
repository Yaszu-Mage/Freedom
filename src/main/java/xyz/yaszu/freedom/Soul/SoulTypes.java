package xyz.yaszu.freedom.Soul;

public enum SoulTypes {
    BaseRed,
    BaseGreen,
    BaseMocha,
    BasePurple,
    BaseBlack,
    BaseNone,
    BaseCafe,
    BaseOrange,
    BaseYellow,
    Yellow,
    BaseBlue,
    Blue,
    Red,
    Green,
    Mocha,
    Purple,
    Black,
    None,
    Cafe,
    Leaf,
    Arcanus,
    Astral,
    BaseCyan,
    Cyan,
    Orange;

    public SoulTypes toBaseVariant() {
        return switch (this) {
            case Red, BaseRed -> BaseRed;
            case Green, BaseGreen -> BaseGreen;
            case Mocha, BaseMocha -> BaseMocha;
            case Purple, BasePurple -> BasePurple;
            case Black, BaseBlack -> BaseBlack;
            case Cafe, BaseCafe -> BaseCafe;
            case Cyan, BaseCyan -> BaseCyan;
            case Orange, BaseOrange -> BaseOrange;
            case Yellow, BaseYellow -> BaseYellow;
            case Blue, BaseBlue -> BaseBlue;
            case None, BaseNone -> BaseNone;
            case Astral -> Astral;
            case Arcanus -> Arcanus;
            case Leaf -> Leaf;
        };
    }
}
