package com.company;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.testing.TestWordSpout;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

/**
 * Created by elson on 4/9/17.
 */

public class ReporterExclamationTopology {

    /**
     * A bolt that adds the exclamation marks '!!!' to word
     */
    public static class ExclamationBolt extends BaseRichBolt
    {
        // To output tuples from this bolt to the next stage bolts, if any
        OutputCollector _collector;

        //********* TO DO 2-of-4
        // place holder to keep the connection to redis


        //********* END 2-of-4

        @Override
        public void prepare(
                Map                     map,
                TopologyContext         topologyContext,
                OutputCollector         collector)
        {
            // save the output collector for emitting tuples
            _collector = collector;

            //********* TO DO 3-of-4
            // instantiate a redis connection

            // initiate the actual connection

            //********* END 3-of-4
        }

        @Override
        public void execute(Tuple tuple)
        {
            // get the column word from tuple
            String word = tuple.getString(0);

            // build the word with the exclamation marks appended

            // emit the word with exclamations
            _collector.emit(tuple, new Values(word + "!!!"));

            //********* TO DO 4-of-4 Uncomment redis reporter
            //long count = 30;
            //redis.publish("WordCountTopology", exclamatedWord.toString() + "|" + Long.toString(count));
            //********* END 4-of-4
        }

        @Override
        public void declareOutputFields(OutputFieldsDeclarer declarer)
        {
            // tell storm the schema of the output tuple for this spout

            // tuple consists of a single column called 'exclamated-word'
            declarer.declare(new Fields("exclamated-word"));
        }
    }

    public static void main(String[] args) throws Exception
    {
        // create the topology
        TopologyBuilder builder = new TopologyBuilder();

        // attach the word spout to the topology - parallelism of 10
        builder.setSpout("word", new TestWordSpout(), 10);

        // attach the exclamation bolt to the topology - parallelism of 3
        builder.setBolt("exclaim1", new ExclamationBolt(), 3).shuffleGrouping("word");

        // attach another exclamation bolt to the topology - parallelism of 2
        builder.setBolt("exclaim2", new ExclamationBolt(), 2).shuffleGrouping("exclaim1");

        // create the default config object
        Config conf = new Config();

        // set the config in debugging mode
        conf.setDebug(true);

        if (args != null && args.length > 0) {

            // run it in a live cluster

            // set the number of workers for running all spout and bolt tasks
            conf.setNumWorkers(3);

            // create the topology and submit with config
            StormSubmitter.submitTopology(args[0], conf, builder.createTopology());

        } else {

            // run it in a simulated local cluster

            // create the local cluster instance
            LocalCluster cluster = new LocalCluster();

            // submit the topology to the local cluster
            cluster.submitTopology("exclamation", conf, builder.createTopology());

            // let the topology run for 30 seconds. note topologies never terminate!
            Thread.sleep(30000);

            // kill the topology
            cluster.killTopology("exclamation");

            // we are done, so shutdown the local cluster
            cluster.shutdown();
        }
    }
}