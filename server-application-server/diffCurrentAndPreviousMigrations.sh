#!/opt/local/bin/ruby

versions = `ls source/sql/postgresql`.map { |dir| dir.to_i }
@curr = versions.max
@prev = @curr - 1

@curr = "%03d" % @curr
@prev = "%03d" % @prev

puts "Will check the diff beetween versions #{@prev} and #{@curr}"

def copyDir(dir)
  files = Dir[dir].map {|f| File.expand_path f }
  files.each do |f|
    new_f = f.gsub(@prev, @curr)
     diff_files = " #{f} #{new_f}"
     system("git diff --no-index" + diff_files)  
  end
end

copyDir("source/sql/postgresql/#{@prev}/*")
copyDir("source/sql/generic/#{@prev}/*")
copyDir("sourceTest/sql/postgresql/#{@prev}/*")






