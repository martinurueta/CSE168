import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import java.util.Arrays; // need to be able to resize our array that was used on line 53

public class TermFrequency {
    public static class TokenizerMapper
       extends Mapper<Object, Text, Text, Text>{ // input text , output text , text

    private Text word = new Text(); // create word

    public void map(Object key, Text value, Context context
                    ) throws IOException, InterruptedException {
      StringTokenizer itr = new StringTokenizer(value.toString()); //extracts the line on the txt file
      int i = 0; // create this to only run a function once
      Text id = new Text(); // create variable to know what line is the word from
      while (itr.hasMoreTokens()) {
        word.set(itr.nextToken()); //the first set of word before the next space
        if (i == 0){ // this only runs once and to figure out what is the index of that line
                String temp_word = word.toString(); // turn Text into a string
                String[] array = temp_word.split(","); // to spilt up to find the index example "1,(word)" into a string array ["1","(word)"]
                word = new Text(array[1]); // turn string of word into Text variable
                id = new Text(array[0]); // turn string index into Text variable
                context.write(id, word); // emit(word, index)
        }
        else{
                context.write(id, word); // emit(word, index)
        }
        i++;

      }
    }
  }
  public static class IntSumReducer
       extends Reducer<Text,Text,Text,Text> {
    private Text result = new Text(); // result is the total # of that word appear in that line 
    public void reduce(Text key, Iterable<Text> values,
                       Context context
                       ) throws IOException, InterruptedException {
                        for (Text val : values){
                            String input = val.toString(); // turn Text id to string input
                            
                         }

    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "Term Frequency");
    job.setJarByClass(TermFrequency.class);
    job.setMapperClass(TokenizerMapper.class);
//    job.setCombinerClass(IntSumReducer.class);
    job.setReducerClass(IntSumReducer.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(Text.class);
    FileInputFormat.addInputPath(job, new Path(args[0]));
    FileOutputFormat.setOutputPath(job, new Path(args[1]));
    System.exit(job.waitForCompletion(true) ? 0 : 1);
  }
}