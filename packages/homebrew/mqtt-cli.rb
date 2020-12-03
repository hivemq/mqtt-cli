class MqttCli < Formula
  desc "@@description@@"
  homepage "https://www.hivemq.com"
  url "https://github.com/hivemq/mqtt-cli/releases/download/v@@version@@/@@filename@@"
  sha256 "@@shasum@@"
  # depends_on :java => "1.8+"

  def install
    inreplace "brew/mqtt", "##PREFIX##", "#{prefix}/mqtt-cli-@@version@@.jar"
    prefix.install "brew/mqtt-cli-@@version@@.jar"
    bin.install "brew/mqtt"
  end

  test do
    system "false"
  end
end
