package com.example.v;

public interface HighWayPatrol {
    interface Command { }
    class Violation implements Command{
        private final String licensePlate;
        private final int speed;
        private final int maxSpeed;

        public Violation(String licensePlate, int speed, int maxSpeed) {
            this.licensePlate = licensePlate;
            this.speed = speed;
            this.maxSpeed = maxSpeed;
        }

        public String getLicensePlate() {
            return licensePlate;
        }

        public int getSpeed() {
            return speed;
        }

        public int getMaxSpeed() {
            return maxSpeed;
        }
    }
    class WithinLimit implements Command{
        private final String licensePlate;
        private final int speed;
        private final int maxSpeed;

        public WithinLimit(String licensePlate, int speed, int maxSpeed) {
            this.licensePlate = licensePlate;
            this.speed = speed;
            this.maxSpeed = maxSpeed;
        }

        public String getLicensePlate() {
            return licensePlate;
        }

        public int getSpeed() {
            return speed;
        }

        public int getMaxSpeed() {
            return maxSpeed;
        }
    }
}
