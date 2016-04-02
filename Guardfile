# A sample Guardfile
# More info at https://github.com/guard/guard#readme

## Uncomment and set this to only include directories you want to watch
# directories %w(app lib config test spec features) \
#  .select{|d| Dir.exists?(d) ? d : UI.warning("Directory #{d} does not exist")}

## Note: if you are using the `directories` clause above and you are not
## watching the project directory ('.'), then you will want to move
## the Guardfile to a watched dir and symlink it back, e.g.
#
#  $ mkdir config
#  $ mv Guardfile config/
#  $ ln -s config/Guardfile .
#
# and, you'll have to watch "config/Guardfile" instead of "Guardfile"

require 'pty'

guard 'livereload' do
  watch(%r{target/karma-test.js})
  watch(%r{target/resources/css/.+\.css})
  watch(%r{target/resources/cljs/.+\.js})
end

def run_protractor
  cmd = "lein protractor"

  begin
    PTY.spawn(cmd) do |stdout, stdin, pid|
      begin
        stdout.each { |line| print line }
      rescue Errno::EIO
      end
    end
  rescue PTY::ChildExited

  end
end

guard :shell do
  watch(%r{specs/.+\.cljs?}) do
    `lein with-profile e2e cljsbuild once`
  end
end

guard :shell do
  # Protractor Config
  watch(%r{protractor.config.js}) {run_protractor}

  # Step definitions
  watch(%r{target/protractor-tests.js}) {run_protractor}

  # Features
  watch(%r{features/.+\.feature}) {run_protractor}

  # Page templates
  watch(%r{resources/templates/.+\.edn}) {run_protractor}
end
