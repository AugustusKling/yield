yield
=====

Extensible, lean event processing.

Demonstrates a simple CEP implementation. Basic concepts are implemented but the set of high-level functions is still limited. Functions can be provided though using the `function` call via sub-classes of `FunctionConfig` from config files.

Executing yield without parameters prints a concise function reference.

Example usage (configuration file content):
```
# Watch a file for changes.
watch "/var/log/sample.log"
# Merge indented lines.
combine
# Read lines as JSON object or convert them if the former fails.
toJSON
# Apply a regular expression to split up the log event's message.
grok message ^(?<time>[^ ]+) (?<level>\w+)\s+\[(?<module>[^\]]+)\] (?<message>.+)$
# Discard everything but errors. level and module are properties in the JSON event.
where level="ERROR" and module contains "democomponent"

# Save remaining events to a file, one JSON object per event.
save "/tmp/filtered.json
```
