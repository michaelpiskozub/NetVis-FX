package netvis.model.filters;

import netvis.model.Packet;
import netvis.model.PacketFilter;
import netvis.ui.controlsfx.RangeSlider;

public class PortFilter implements PacketFilter {
    private RangeSlider sourcePortSlider;
    private RangeSlider destinationPortSlider;

    public PortFilter(RangeSlider sourcePortSlider, RangeSlider destinationPortSlider) {
        this.sourcePortSlider = sourcePortSlider;
        this.destinationPortSlider = destinationPortSlider;
    }

    @Override
    public boolean isFilterTestPassed(Packet packet) {
        return (packet.SOURCE_PORT >= sourcePortSlider.getLowValue()
                && packet.SOURCE_PORT <= sourcePortSlider.getHighValue()
                && packet.DESTINATION_PORT >= destinationPortSlider.getLowValue()
                && packet.DESTINATION_PORT <= destinationPortSlider.getHighValue()
        );
    }

    @Override
    public String getName() {
        return "Port Filter";
    }
}
