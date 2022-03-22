package com.enderio.base.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class AttractionUtil {

    /**
     * Pulls the entity closer to the given BlockPos. Once it is in inside the collision distance, the function will return true, otherwise it returns false.
     * @param entity
     * @param pos
     * @param speed
     * @param speed4
     * @param collisionDistanceSq
     * @return if the entity is inside the collision distance.
     */
    public static boolean hasReachedPos(Entity entity , BlockPos pos, double speed, double speed4, double collisionDistanceSq) {
       return hasReachedPos(entity, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, speed, speed4, collisionDistanceSq);
    }
    
    /**
     * Pulls the entity closer to the given x, y and z coordinates. Once it is in inside the collision distance, the function will return true, otherwise it returns false.
     * @param entity
     * @param x
     * @param y
     * @param z
     * @param speed
     * @param speed4
     * @param collisionDistanceSq
     * @return if the entity is inside the collision distance.
     */
    public static boolean hasReachedPos(Entity entity , double x, double y, double z, double speed, double speed4, double collisionDistanceSq) {
        if (entity == null) { //If the entity no longer exists, return false
            return false;
        }
        x -= entity.getX();
        y -= entity.getY();
        z -= entity.getZ();
        
        double distanceSq = x * x + y * y + z * z;
        
        if (distanceSq < collisionDistanceSq) {
            return true;
        } else {
            double adjustedSpeed = speed4 / distanceSq;
            Vec3 mov = entity.getDeltaMovement();
            double deltaX = mov.x + x * adjustedSpeed;
            double deltaZ = mov.z + z * adjustedSpeed;
            double deltaY;
            if (y > 0) {
              //if items are below, raise them to player level at a fixed rate
                deltaY = 0.12;
            } else {
              //Scaling y speed based on distance works poorly due to 'gravity' so use fixed speed
                deltaY = mov.y + y * speed;
            }
            entity.setDeltaMovement(deltaX, deltaY, deltaZ);
            return false;
        }
    }
}
