package netvis.model.input.tshark;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class PacketStreamThread extends Thread {
    private InputStream in;
    private boolean finished;
    private List<String[]> packetLines;

    public PacketStreamThread(InputStream in) {
        this.in = in;
        finished = false;
        packetLines = new ArrayList<>();
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int getPacketLinesSize() {
        return packetLines.size();
    }

    public String[] getPacketLine(int index) {
        return packetLines.get(index);
    }

    public String[] getLastPacketLine() {
        return packetLines.get(packetLines.size() - 1);
    }

    public Iterator<String[]> getPacketLinesIterator() {
        return packetLines.iterator();
    }

    @Override
    public void run() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(in), 1);
            String line;
            while ((line = br.readLine()) != null && !finished) {
                ArrayList<String> splitLine =
                        new ArrayList<>(Arrays.asList(line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)")));
                if (splitLine.size() == 10) {
                    splitLine.add("");
                }
                String[] filteredLine = splitLine.stream().map(l -> {
                    if (!l.equals("")) {
                        return l.substring(1, l.length() - 1);
                    }
                    return "";
                }).toArray(String[]::new);

                packetLines.add(filteredLine);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                assert br != null;
                br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
