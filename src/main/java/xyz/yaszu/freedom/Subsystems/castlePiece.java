package xyz.yaszu.freedom.Subsystems;
import xyz.yaszu.freedom.Subsystems.RedCastleManager.Directions;
import java.util.ArrayList;
import java.util.List;

public class castlePiece {
    String schematicName = "";
    List<RedCastleManager.Directions> connections;
    List<RedCastleManager.Directions> SecondfloorConnections;
    public boolean isSecondFloor = false;
    public int rotation = 0; // 0, 90, 180, 270

    public castlePiece(String castlePieceName, List<RedCastleManager.Directions> connections, List<RedCastleManager.Directions> SecondfloorConnections, boolean isSecondFloor) {
        this.schematicName = castlePieceName;
        this.connections = connections;
        this.SecondfloorConnections = SecondfloorConnections;
        this.isSecondFloor = isSecondFloor;
    }

    public castlePiece(castlePiece other) {
        this.schematicName = other.schematicName;
        this.connections = new ArrayList<>(other.connections);
        this.SecondfloorConnections = new ArrayList<>(other.SecondfloorConnections);
        this.isSecondFloor = other.isSecondFloor;
        this.rotation = other.rotation;
    }

    public castlePiece rotated(int angle) {
        castlePiece rotated = new castlePiece(this);
        rotated.rotation = (this.rotation + angle) % 360;
        rotated.connections = rotateDirections(this.connections, angle);
        rotated.SecondfloorConnections = rotateDirections(this.SecondfloorConnections, angle);
        return rotated;
    }

    private static List<Directions> rotateDirections(List<Directions> dirs, int angle) {
        List<Directions> rotated = new ArrayList<>();
        int steps = (angle / 90) % 4;
        for (Directions dir : dirs) {
            Directions d = dir;
            for (int i = 0; i < steps; i++) {
                d = switch (d) {
                    case North -> Directions.East;
                    case East -> Directions.South;
                    case South -> Directions.West;
                    case West -> Directions.North;
                    case Up -> Directions.Up;
                    case Down -> Directions.Down;
                };
            }
            rotated.add(d);
        }
        return rotated;
    }
}
