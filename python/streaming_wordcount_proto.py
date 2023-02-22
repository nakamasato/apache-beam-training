import argparse
import logging

import apache_beam as beam
from apache_beam.examples.wordcount_with_metrics import WordExtractingDoFn
from apache_beam.options.pipeline_options import (PipelineOptions,
                                                  SetupOptions,
                                                  StandardOptions)
from apache_beam.transforms import window
from wordcount.wordcount_pb2 import WordCount


def run(argv=None, save_main_session=True):
    """Build and run the pipeline."""
    parser = argparse.ArgumentParser()
    _, pipeline_args = parser.parse_known_args(argv)

    # We use the save_main_session option because one or more DoFn's in this
    # workflow rely on global context (e.g., a module imported at module level).
    pipeline_options = PipelineOptions(pipeline_args)
    pipeline_options.view_as(SetupOptions).save_main_session = save_main_session
    pipeline_options.view_as(StandardOptions).streaming = False
    with beam.Pipeline(options=pipeline_options) as p:
        # Input data
        lines = p | beam.Create(
            ["This is a test message.", "Another message also has some text."]
        )

        # Count the occurrences of each word.
        def count_ones(word_ones):
            (word, ones) = word_ones
            return (word, sum(ones))

        counts = (
            lines
            | "split" >> (beam.ParDo(WordExtractingDoFn()).with_output_types(str))
            | "pair_with_one" >> beam.Map(lambda x: (x, 1))
            | beam.WindowInto(window.FixedWindows(15, 0))
            | "group" >> beam.GroupByKey()
            | "count" >> beam.Map(count_ones)
        )

        # Format the counts into a PCollection of WordCount.
        def format_result(word_count):
            wc = WordCount()
            wc.word, wc.count = word_count
            return wc

        output = counts | "format" >> beam.Map(format_result)

        # Write to file
        output | beam.io.WriteToText("output/wordcount-")


if __name__ == "__main__":
    logging.getLogger().setLevel(logging.INFO)
    run()
