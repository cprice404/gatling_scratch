Recording a Puppet agent/master HTTPS session using Gatling Recorder
--------------------------------------------------------------------

Gatling Recorder is a proxy server that can record HTTP requests and responses,
and use them to generate re-usable code that will reproduce that sequence of
requests and responses for load testing.  What we're going to do here is to
run a puppet agent and record all of it's communication with a puppet master,
and capture that so that we can tweak it and do various kinds of puppet master
load testing.

To run gatling recorder (proxy for capturing HTTP requests and generating
an initial gatling scenario):

    mvn package
    sh ./target/appassembler/bin/gatling-recorder.sh

This will bring up a GUI for the Gatling Recorder proxy.  You can pretty
much leave everything set to the default values (definitely leave the ports!),
but you can change the package/class name and output dir as you see fit.  (When
you finish a recording session Gatling will generate Scala code for reproducing
your session, and the path and filenames of this output will correspond to your
choices here.)  Once you're all set on that stuff, just hit 'Start!' to fire
up the proxy.

Next, you'll probably want to record a puppet agent/master session.  This is
a bit tricky because the gatling proxy is basically going to be a
man-in-the-middle and will cause SSL headaches.  It ships with a self-signed
certificate, so with some hackery (basically, copying that cert around
everywhere), you can get puppet to tolerate it.  I've attempted to capture the
necessary certs and config files in the `src/test/resources` directory; you will
need to add an entry for 'Gatling' to your `/etc/hosts` file (probably just add it to the 127.0.0.1 line).  Then you should be able to run the master with:

    --confdir ./src/test/resources/gatling/recorder/puppet/master/conf --certname gatling

When prompted for PEM passphrases just type 'gatling'.

Run the agent with:

    --confdir ./src/test/resources/gatling/recorder/puppet/agent/conf --certname localhost --server Gatling

That should work with the default gatling config.  vardirs are set from inside
puppet.conf to point into the `./target` directory, but you probably won't
need anything out of them.

Once you've completed an agent run, you should see several events / requests /
responses in the Gatling Recorder GUI.  Click 'Stop and Save' and it will
generate the code you'll need for repro'ing the HTTPS request/response series.

Load Testing a Webrick Puppet Master
------------------------------------

Start the puppet master with:

    --confdir ./src/test/resources/gatling/simulation/puppet/master/conf --certname localhost

Run the load test with:

    mvn gatling:execute

The parameters of the test (number of users, etc.) can be tweaked in the code
found in `src/test/scala/com/puppetlabs/puppet/master/loadtest/PuppetMasterLoadTest.scala`

The interesting parameters are the arguments to `repeat` (~line 51) and the
arguments to `users` and `ramp` (last line of code).

Load Testing a Passenger Puppet Master
--------------------------------------

First: get a clean master/agent run working using the master confdir listed in
the previous section, to make sure all of the SSL certs are set up correctly.
Then shut down the master.

As root:

    update.rc-d apache2 disable
    service apache2 stop
    ln -s `pwd`/src/test/resources/gatling/simulation/puppet/master/apache2/puppetmaster_vhost /etc/apache2/sites-available
    a2ensite puppetmaster_vhost
    a2enmod ssl
    a2enmod headers
    apt-get install libcurl4-openssl-dev  apache2-prefork-dev libapr1-dev libaprutil1-dev
    gem install rack passenger
    passenger-install-apache2-module

As user:

    sudo RUBYLIB=${PUPPET_SRC}/lib:${FACTER_SRC}/lib GATLING_SCRATCH_ROOT=`pwd` apache2ctl start

    mvn gatling:execute

Load Testing a Jetty No-op Server
---------------------------------

    mvn package
    sh ./target/appassembler/bin/jetty-hello-world.sh

In another shell:

    mvn gatling:execute

Load Testing a Clojure/Ring No-op Server
----------------------------------------

    mvn package
    sh ./target/appassembler/bin/ring-hello-world.sh

In another shell:

    mvn gatling:execute


Load Testing an Akka/Spray No-op Server
----------------------------------------

    mvn package
    sh ./target/appassembler/bin/spray-hello-world.sh

In another shell:

    mvn gatling:execute

