package com.example.taurerstand.altimeter;

import android.util.Log;

import java.util.Arrays;

/**
 * Created by TaurerStand on 22.02.2018.
 */

public class BlockingQueueFIFO {

    int[] queue;
    int index;
    int size;

    public BlockingQueueFIFO(int size){
        queue = new int[size];
        this.size = size;
        index = 0;
    }

    public void add(int value){
        synchronized (queue){
            queue[index] = value;
            index = (index + 1) % size;
        }
    }

    public double getAVG(){
        double avg, sum = 0.0;

        synchronized (queue){
            for (int i = 0; i < queue.length; i++) {
                sum += queue[i];
            }
        }

        avg = sum / size;
        return avg/12.0;
    }

    public double getMedian(){
        int medianIndex = Math.round(size/2);
        int medianValue;

        synchronized (queue){
            Arrays.sort(queue);
            medianValue = queue[medianIndex];
        }
        return medianValue / 12.0;
    }
}
