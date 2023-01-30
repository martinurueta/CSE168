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

public class TermFreq {
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
                context.write(word, id); // emit(word, index)
        }
        else{
                context.write(word, id); // emit(word, index)
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
        int[] id_total = new int[1]; // make an array to hold the total # that the word appear in each line
        for (Text val : values){
                String input = val.toString(); // turn Text id to string input
                int id = Integer.parseInt(input); // turn string input to int id
                if (id_total.length < id){ // if the index is greater than the length of the array that is defaulted to size of 1
                    id_total = Arrays.copyOf(id_total, id); // we change the size of the array to the size of that id
                }
                id_total[id-1] = id_total[id-1] + 1; // we add +1 to the total number # that word appear in each line (array [0] = line 1 on the text, array [1]= line 2 on the text, ...)
        }
        for (int i = 0; i < id_total.length; i++){
                Integer g = new Integer(i+1); // turn int i+1 to a integer g
                String output_id = g.toString(); // turn integer g to string output_id to get the index of that line
                Integer j = new Integer(id_total[i]); // turn int id_total[i] to an integer j
                String output_sum = j.toString(); // turn integer j to output_sum to get the # of word that appear on that line
                result = new Text(output_id+", "+output_sum); // combine output_id and , and ouput_sum == "index, #ofWordAppear" and turn it into Text result
                if (id_total[i] > 0){// if the number of times that word appears is 0 or less then we dont emit
                        context.write(key, result); // emit(word, (index, #ofWordAppear))
                }
        }

    }
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = new Configuration();
    Job job = Job.getInstance(conf, "TermFreq");
    job.setJarByClass(TermFreq.class);
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
