package com.steffbeard.totalwar.nations.misc.trails;

import java.util.Iterator;
import java.util.HashMap;
import org.bukkit.Material;

import com.steffbeard.totalwar.nations.Main;

public class Link {
	
    private Material mat;
    private int decay;
    private int id;
    private int chanceoccur;
    private byte dataValue;
    private Link next;
    public static HashMap<Main.TypeAndData, Link> matLinks;
    
    public Link(final Material material, final byte dataValue, final int decaynum, final int chance, final int linknumb, final Link nextlink) {
        this.mat = material;
        this.dataValue = dataValue;
        this.decay = decaynum;
        this.id = linknumb;
        this.chanceoccur = chance;
        this.next = nextlink;
    }
    
    public Material getMat() {
        return this.mat;
    }
    
    public int decayNumber() {
        return this.decay;
    }
    
    public byte getDataValue() {
        return this.dataValue;
    }
    
    public int identifier() {
        return this.id;
    }
    
    public Link getNext() {
        return this.next;
    }
    
    public int chanceOccurance() {
        return this.chanceoccur;
    }
    
    public Link getFromMat(final Material mat) {
        if (Link.matLinks != null) {
            for (final Main.TypeAndData mats : Link.matLinks.keySet()) {
                if (mats.mat == mat) {
                    return Link.matLinks.get(mats);
                }
            }
        }
        return null;
    }
    
    static {
        Link.matLinks = new HashMap<Main.TypeAndData, Link>();
    }
}
