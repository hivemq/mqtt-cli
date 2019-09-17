class HivemqCli < Formula
  desc "@@description@@"
  homepage "https://www.hivemq.com"
  url "https://www.hivemq.com/tests/@@filename@@"
  sha256 "@@shasum@@"
  depends_on :java => "1.8+"

  def install
    inreplace "brew/hivemq-cli", "##PREFIX##", "#{prefix}"
    prefix.install "hivemq-cli.jar"
    bin.install "brew/hivemq-cli"
  end

  test do
    system "false"
  end
end