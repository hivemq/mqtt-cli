class HivemqCli < Formula
  desc "@@description@@"
  homepage "https://www.hivemq.com"
  url "https://www.hivemq.com/tests/@@filename@@"
  sha256 "@@shasum@@"
  depends_on :java => "1.8+"

  def install
    inreplace "brew/mqtt-cli", "##PREFIX##", "#{prefix}"
    prefix.install "mqtt-cli.jar"
    bin.install "brew/mqtt-cli"
  end

  test do
    system "false"
  end
end