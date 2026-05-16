package com.heaser.pipeconnector.utils;

import com.heaser.pipeconnector.constants.ComponentDataTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

public class NodeParameter {
    public BlockPos position;
    public Direction direction;

    public NodeParameter(BlockPos position, Direction direction) {
        this.position = position;
        this.direction = direction;
    }

    public NodeParameter(CompoundTag nodeTag) {
        int x = nodeTag.getIntOr(ComponentDataTags.kPipeConnectorNodePositionX, 0);
        int y = nodeTag.getIntOr(ComponentDataTags.kPipeConnectorNodePositionY, 0);
        int z = nodeTag.getIntOr(ComponentDataTags.kPipeConnectorNodePositionZ, 0);
        this.position = new BlockPos(x, y, z);
        if (!nodeTag.contains(ComponentDataTags.kPipeConnectorNodeDirection)) {
            this.direction = null;
        } else {
            this.direction = Direction.from3DDataValue(nodeTag.getByteOr(ComponentDataTags.kPipeConnectorNodeDirection, (byte) 0));
        }
    }


    public boolean equals(NodeParameter other) {
        if ( this.direction == null || other.direction == null ) {
            return this.position.equals(other.position);
        }
        boolean positionEquals = this.position.equals(other.position);
        boolean directionEquals = this.direction.equals(other.direction);
        return positionEquals && directionEquals;
    }

    public BlockPos getRelativePosition() {
        if (direction == null) {
            return this.position;
        } else {
            return this.position.relative(direction);
        }
    }
}
