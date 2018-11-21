# DWRTC - Distributed WebRTC Signalling

[![Sonar Rating](https://sonarcloud.io/api/project_badges/measure?project=dwrtc&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=dwrtc)
[![CircleCI](https://circleci.com/gh/dwrtc/dwrtc.svg?style=svg)](https://circleci.com/gh/dwrtc/dwrtc)

Project Page: https://dwrtc.net/

WebSocket API documentation: https://docs.dwrtc.net/dwrtc/ch.hsr.dsl.dwrtc.websocket/

Full Kdoc documentation: https://docs.dwrtc.net/

## Usage

```sh
$ docker-compose up -d
$ docker-compose ps

    Name               Command           State            Ports
------------------------------------------------------------------------
dwrtc_node1_1   java -jar /app/app.jar   Up      0.0.0.0:PORT1->7000/tcp
dwrtc_node2_1   java -jar /app/app.jar   Up      0.0.0.0:PORT2->7000/tcp
```

Connect to `localhost:PORT1` for node1 and `localhost:PORT2` for node2 via a web browser.

## Options

Options are loaded using [Konfig](https://github.com/npryce/konfig). The priorities are:

1. Command Line Properties overrides
2. Environment Variables overrides
3. Property file `defaults.properties`

Konfig supports two syntaxes. "dot"-syntax (`http.port`) for Command Line and Property File, and "underscore"-syntax for Environment variables. The two options are shown below.

The options are:

* Peer Port (Int): `peer.port`/`PEER_PORT`: port the TomP2P peer runs on
* Webserver IP (String): `http.ip`/`HTTP_IP`: the IP the web server binds to (e.g. "127.0.0.1", "localhost", "0.0.0.0")
* Webserver Port (Int): `http.port`/`HTTP_PORT`: the port the web server binds to
* Bootstrap Peer (comma-separated list of `HOST:PORT` pairs): the list of peers to bootstrap to (e.g. "1.1.1.1:4000,2.2.2.2:3000")

## Introduction

> Coming

## Code Samples

> Coming

## Installation

> Coming
