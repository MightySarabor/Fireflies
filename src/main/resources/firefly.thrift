namespace java firefly

service FireflyService {
    void sendState(1: i32 clientId, 2: i32 state),
    list<i32> getNeighborStates(1: i32 gridX, 2: i32 gridY),
}
