package netvis.view.util.jogl.comets;

import netvis.model.Packet;

import java.util.ArrayList;

public class Candidate {
    // How close the client is to the internal network 0-closest 10-furthest
    public int proximity;

    // How much data it send throughout the last interval
    public int datasize;

    // Source and destination
    public String sip;
    public String dip;

    private ArrayList<Packet> plist;

    public Candidate(int prox, int dat, String s, String d) {
        proximity = prox;
        datasize = dat;

        sip = s;
        dip = d;

        plist = new ArrayList<>();
    }

    public void RegisterPacket (Packet p) {
        datasize += p.LENGTH;
        plist.add(p);
    }

    public ArrayList<Packet> getWaitingPackets() {
        return plist;
    }

    public void resetWaitingPackets() {
        plist = new ArrayList<>();
    }
}
