# NetVis-FX

NetVis FX is a tool that allows you to visualize network traffic, so that you can see anomalies and other suspicious traffic flows clearer. It was written as a part of MSc Thesis in 2015 at the University of Oxford. The project is a fork of [NetVis](https://github.com/ryft/NetVis).

## Visualizations

### Galaxy

Galaxy is a visualization that consists of the sun, and planets positioned around a circle. Planets represent packet features that include: source IP, destination IP, source port, destination port, and protocol. They can be selected from the Controls menu. The sun is the origin point, where packets, represented as cubes, start their journey to a target planet, which is selected based on the feature of a packet, e.g. a packet using the DNS protocol is headed towards the planet labeled as DNS.

![Galaxy Visualization](https://raw.githubusercontent.com/michaelpiskozub/NetVis-FX/master/images/NetVisFXGalaxy.jpg)

Packets travel with speeds based on their length. The larger the packet the slower it moves towards the planet. Length is also encoded in colors of packets. Three ranges are deﬁned: 0-500 (green), 500-1000 (yellow) and 1000-1500 (cyan).
In IP mode, the range of the ﬁrst byte of the address is divided between planets. In Port mode the whole range (0-65535) is divided between planets.

<p align="center"><img src="https://raw.githubusercontent.com/michaelpiskozub/NetVis-FX/master/images/NetVisFXGalaxy3DView.jpg" alt="Galaxy Zoomed View" width="400"/></p>

Galaxy is three-dimensional. The sun is closer to the camera than the planets, but equal distances between them are maintained to not mislead users. The position of the camera can be changed using a combination of keyboard and mouse shortcuts (see [Controls](https://github.com/michaelpiskozub/NetVis-FX#controls)).

### Parallel Galaxy

Parallel Galaxy is a visualization that implements parallel coordinates using the logic from the Galaxy visualization. The sun is now positioned on the very left side. Five columns of planets are located next to each other. Every column consists of 10 planets. Packets are released from the sun and ﬂy to a planet from every column representing its respective parameters.

![Parallel Galaxy](https://raw.githubusercontent.com/michaelpiskozub/NetVis-FX/master/images/NetVisFXParallelGalaxy.jpg)

All other features are analogous to the Galaxy visualization, apart from labels, which can be toggled in the Controls menu. When they are shown, the visualization feels too cluttered and packets are not as easily recognizable. Hence the labels should be used for reminding a user what given planets represent.

## Features

### Easily Distinguish Packets

![Galaxy BBC Video](https://raw.githubusercontent.com/michaelpiskozub/NetVis-FX/master/images/NetVisFXGalaxyBBCVideo.jpg)

Galaxy shows us different types of packages based on their size. If we visit a website containing a video, we can state with high probability that clusters of long packets carry video data and smaller ones represent queries, e.g. connection initialization.

### Explore Port Scan Granularity

![Galaxy Port Scan](https://raw.githubusercontent.com/michaelpiskozub/NetVis-FX/master/images/NetVisFXGalaxyPortScan.jpg)

A starfish-shaped pattern is identified with a port scan and it shows which port ranges are queried most frequently.

![Parallel Galaxy Port Scan](https://raw.githubusercontent.com/michaelpiskozub/NetVis-FX/master/images/NetVisFXParallelGalaxyPortScan.jpg)

Same type of attack presented as a parallel galaxy.

### Detect Flood Attacks

![Parallel Galaxy JRE Overflow](https://raw.githubusercontent.com/michaelpiskozub/NetVis-FX/master/images/NetVisFXParallelGalaxyJREOverflow.jpg)

A polygonal chain, formed by packets, indicates a flood attack, such as DDoS. Its thickness or degree of being connected is attributed to the amount of packets forming it.

## Controls

Move Camera: `right click & drag up/down` - up/down, `W` - forward, `S` - backward, `A` - left, `D` - right, `left click & drag` - look around, `R` - reset camera, `Shift` - increase speed

## Dependencies

- Apache Commons IO 2.4 
- Apache Commons Lang 3.4 
- gluegen-rt 
- gluegen-rt-natives-linux-amd64 
- gluegen-rt-natives-macosx-universal 
- gluegen-rt-natives-windows-amd64 
- Hamcrest Core 1.3 
- JCSG 
- jogl-all 
- jogl-all-natives-linux-amd64 
- jogl-all-natives-macosx-universal 
- jogl-all-natives-windows-amd64 
- JUnit 4.12 
- Poly2tri Core 0.1.0

## License

The GNU General Public License v3.0