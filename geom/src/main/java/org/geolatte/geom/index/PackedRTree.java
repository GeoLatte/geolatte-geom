/*
 * This file is part of the GeoLatte project.
 *
 *     GeoLatte is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GeoLatte is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with GeoLatte.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2010 - 2026 and Ownership of code is shared by:
 * Geovise bvba - Generaal Eisenhowerlei 9 - 2140 Antwerpen (http://www.geovise.com)
 */

package org.geolatte.geom.index;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * A static, bulk-loaded R-tree over items with 2D axis-aligned bounding boxes.
 *
 * <p>The tree is packed with the Sort-Tile-Recursive (STR) algorithm: the items are sorted on the X-coordinate of
 * the centre of their bounding boxes and cut into vertical slices, after which each slice is sorted on the
 * Y-coordinate and cut into nodes. The same is repeated for the nodes, level by level, until a single root
 * remains. This yields a well-balanced tree with nearly full nodes. Building takes O(n log n) time.</p>
 *
 * <p>Instances are created with a {@link Builder}, and are immutable once built: items cannot be added or
 * removed. An instance can therefore be queried concurrently from multiple threads.</p>
 *
 * <p>Bounding boxes are closed: boxes that only touch along an edge or at a corner are considered to
 * intersect.</p>
 *
 * <p>Example:</p>
 * <pre>{@code
 * PackedRTree<Feature> index = PackedRTree.<Feature>builder()
 *         .add(feature1, 0, 0, 10, 10)
 *         .add(feature2, 5, 5, 20, 20)
 *         .build();
 * List<Feature> hits = index.query(8, 8, 9, 9);
 * }</pre>
 *
 * @param <T> the type of the indexed items
 */
public final class PackedRTree<T> {

    /**
     * The default maximum number of children per node.
     */
    public static final int DEFAULT_NODE_CAPACITY = 16;

    private final Node root; // null if the tree is empty
    private final int size;

    private PackedRTree(Node root, int size) {
        this.root = root;
        this.size = size;
    }

    /**
     * Returns a builder for a tree with the {@link #DEFAULT_NODE_CAPACITY default node capacity}.
     *
     * @param <T> the type of the indexed items
     * @return a new {@code Builder}
     */
    public static <T> Builder<T> builder() {
        return new Builder<>(DEFAULT_NODE_CAPACITY);
    }

    /**
     * Returns a builder for a tree with the specified maximum number of children per node.
     *
     * @param nodeCapacity the maximum number of children per node
     * @param <T>          the type of the indexed items
     * @return a new {@code Builder}
     * @throws IllegalArgumentException if nodeCapacity is less than 2
     */
    public static <T> Builder<T> builder(int nodeCapacity) {
        if (nodeCapacity < 2) {
            throw new IllegalArgumentException("Node capacity must be at least 2, but was " + nodeCapacity);
        }
        return new Builder<>(nodeCapacity);
    }

    /**
     * Returns the number of items in this tree.
     *
     * @return the number of items in this tree
     */
    public int size() {
        return size;
    }

    /**
     * Returns true if this tree contains no items.
     *
     * @return true if this tree contains no items
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Passes every item whose bounding box intersects the specified box to the visitor, until the visitor
     * returns false.
     *
     * <p>Items are visited in no particular order.</p>
     *
     * @param minX    the minimum X-coordinate of the query box
     * @param minY    the minimum Y-coordinate of the query box
     * @param maxX    the maximum X-coordinate of the query box
     * @param maxY    the maximum Y-coordinate of the query box
     * @param visitor receives the matching items; returns true to continue, or false to stop the query
     * @return true if all matching items were visited, false if the visitor stopped the query
     * @throws IllegalArgumentException if the query box is invalid (a coordinate is NaN, or a minimum exceeds the
     *                                  corresponding maximum)
     */
    public boolean query(double minX, double minY, double maxX, double maxY, Predicate<? super T> visitor) {
        checkBox(minX, minY, maxX, maxY);
        Objects.requireNonNull(visitor, "visitor");
        return root == null || query(root, minX, minY, maxX, maxY, visitor);
    }

    /**
     * Returns the items whose bounding boxes intersect the specified box.
     *
     * @param minX the minimum X-coordinate of the query box
     * @param minY the minimum Y-coordinate of the query box
     * @param maxX the maximum X-coordinate of the query box
     * @param maxY the maximum Y-coordinate of the query box
     * @return the matching items, in no particular order
     * @throws IllegalArgumentException if the query box is invalid (a coordinate is NaN, or a minimum exceeds the
     *                                  corresponding maximum)
     */
    public List<T> query(double minX, double minY, double maxX, double maxY) {
        List<T> result = new ArrayList<>();
        query(minX, minY, maxX, maxY, item -> {
            result.add(item);
            return true;
        });
        return result;
    }

    @SuppressWarnings("unchecked")
    private boolean query(Node node, double minX, double minY, double maxX, double maxY, Predicate<? super T> visitor) {
        for (Bounded child : node.children) {
            if (!child.intersects(minX, minY, maxX, maxY)) continue;
            if (node.isLeaf) {
                if (!visitor.test(((Entry<T>) child).item)) return false;
            } else if (!query((Node) child, minX, minY, maxX, maxY, visitor)) {
                return false;
            }
        }
        return true;
    }

    private static void checkBox(double minX, double minY, double maxX, double maxY) {
        // the negated comparisons also reject NaN values
        if (!(minX <= maxX) || !(minY <= maxY)) {
            throw new IllegalArgumentException(String.format("Invalid bounding box: (%s, %s, %s, %s)",
                    minX, minY, maxX, maxY));
        }
    }

    /**
     * Collects items with their bounding boxes and packs them into a {@code PackedRTree}.
     *
     * <p>A builder can be reused: {@link #build()} does not reset it, so further items can be added and
     * another tree built.</p>
     *
     * @param <T> the type of the indexed items
     */
    public static final class Builder<T> {
        private final int nodeCapacity;
        private final List<Entry<T>> entries = new ArrayList<>();

        private Builder(int nodeCapacity) {
            this.nodeCapacity = nodeCapacity;
        }

        /**
         * Adds an item with its bounding box.
         *
         * @param item the item to index; must not be null
         * @param minX the minimum X-coordinate of the item's bounding box
         * @param minY the minimum Y-coordinate of the item's bounding box
         * @param maxX the maximum X-coordinate of the item's bounding box
         * @param maxY the maximum Y-coordinate of the item's bounding box
         * @return this builder
         * @throws IllegalArgumentException if the bounding box is invalid (a coordinate is NaN, or a minimum
         *                                  exceeds the corresponding maximum)
         */
        public Builder<T> add(T item, double minX, double minY, double maxX, double maxY) {
            Objects.requireNonNull(item, "item");
            checkBox(minX, minY, maxX, maxY);
            entries.add(new Entry<>(item, minX, minY, maxX, maxY));
            return this;
        }

        /**
         * Builds a tree containing all items added so far.
         *
         * @return a new {@code PackedRTree}
         */
        public PackedRTree<T> build() {
            if (entries.isEmpty()) return new PackedRTree<>(null, 0);
            List<Bounded> level = new ArrayList<>(entries);
            boolean isLeafLevel = true;
            do {
                level = packLevel(level, isLeafLevel);
                isLeafLevel = false;
            } while (level.size() > 1);
            return new PackedRTree<>((Node) level.get(0), entries.size());
        }

        /**
         * Groups the specified boxes into nodes, using Sort-Tile-Recursive packing.
         */
        private List<Bounded> packLevel(List<Bounded> boxes, boolean isLeafLevel) {
            int numNodes = (boxes.size() + nodeCapacity - 1) / nodeCapacity;
            int numSlices = (int) Math.ceil(Math.sqrt(numNodes));
            int sliceSize = numSlices * nodeCapacity;
            boxes.sort(Comparator.comparingDouble(Bounded::centerX));
            List<Bounded> parents = new ArrayList<>(numNodes + numSlices);
            for (int s = 0; s < boxes.size(); s += sliceSize) {
                List<Bounded> slice = new ArrayList<>(boxes.subList(s, Math.min(s + sliceSize, boxes.size())));
                slice.sort(Comparator.comparingDouble(Bounded::centerY));
                for (int i = 0; i < slice.size(); i += nodeCapacity) {
                    parents.add(new Node(slice.subList(i, Math.min(i + nodeCapacity, slice.size())), isLeafLevel));
                }
            }
            return parents;
        }
    }

    /**
     * A closed, axis-aligned bounding box.
     */
    private abstract static class Bounded {
        double minX, minY, maxX, maxY;

        final boolean intersects(double qMinX, double qMinY, double qMaxX, double qMaxY) {
            return maxX >= qMinX && qMaxX >= minX && maxY >= qMinY && qMaxY >= minY;
        }

        final double centerX() {
            return (minX + maxX) / 2;
        }

        final double centerY() {
            return (minY + maxY) / 2;
        }
    }

    /**
     * An indexed item with its bounding box.
     */
    private static final class Entry<T> extends Bounded {
        final T item;

        Entry(T item, double minX, double minY, double maxX, double maxY) {
            this.item = item;
            this.minX = minX;
            this.minY = minY;
            this.maxX = maxX;
            this.maxY = maxY;
        }
    }

    /**
     * A tree node. The children of a leaf node are {@code Entry}s, those of other nodes are {@code Node}s.
     */
    private static final class Node extends Bounded {
        final Bounded[] children;
        final boolean isLeaf;

        Node(List<Bounded> children, boolean isLeaf) {
            this.children = children.toArray(new Bounded[0]);
            this.isLeaf = isLeaf;
            minX = minY = Double.POSITIVE_INFINITY;
            maxX = maxY = Double.NEGATIVE_INFINITY;
            for (Bounded child : this.children) {
                minX = Math.min(minX, child.minX);
                minY = Math.min(minY, child.minY);
                maxX = Math.max(maxX, child.maxX);
                maxY = Math.max(maxY, child.maxY);
            }
        }
    }
}
