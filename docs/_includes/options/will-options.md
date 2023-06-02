| Option | Long Version                   | Explanation                                                                                     | Default                    |
|--------|--------------------------------|-------------------------------------------------------------------------------------------------|----------------------------|
| `-Wd`  | `--willDelayInterval`          | Will delay interval in seconds.                                                                 | `0`                        |
| `-We`  | `--willMessageExpiryInterval`  | Lifetime of the will message in seconds. <br> Can be disabled by setting it to `4_294_967_295`. | `4_294_967_295` (Disabled) |
| `-Wm`  | `--willPayload`                | Payload of the will message.                                                                    |                            |
| `-Wq`  | `--willQualityOfService`       | QoS level of the will message.                                                                  | `0`                        |
| `-Wr`  | `--willRetain`                 | Retain the will message.                                                                        | `false`                    |
| `-Wt`  | `--willTopic`                  | Topic of the will message.                                                                      |                            |
| `-Wcd` | `--willCorrelationData`        | Correlation data of the will message.                                                           |                            |
| `-Wct` | `--willContentType`            | Description of the will message's content.                                                      |                            |
| `-Wpf` | `--willPayloadFormatIndicator` | Payload format can be explicitly specified as `UTF8` else it may be `UNSPECIFIED`.              |                            |
| `-Wrt` | `--willResponseTopic`          | Topic name for a response message.                                                              |                            |
| `-Wup` | `--willUserProperties`         | A user property of the will message.                                                            |                            |
