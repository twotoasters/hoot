# hoottests.rb
require 'sinatra'
require 'json'

helpers do
  def request_headers
    env.inject({}){|acc, (k,v)| acc[$1] = v if k =~ /^http_(.*)/i; acc}
  end  
end

get '/' do
  '{"test":"This is a test"}'
end

get '/error/:error' do
  status params[:error].to_i
end

get '/params' do
  params.to_json
end

get '/headers.and.params' do
  {"params" => params, "headers" => request_headers["HOOT_TEST_HEADER"]}.to_json
end

get '/headers' do
  {"headers" => request_headers["HOOT_TEST_HEADER"]}.to_json
end

get '/wait' do
  sleep 10
  '{"test":"This is a test"}'
end

delete '/' do
    '{"test":"This is a test"}'
end

delete '/error/:error' do
    status params[:error].to_i
end

delete '/params' do
    params.to_json
end

delete '/headers.and.params' do
    {"params" => params, "headers" => request_headers["HOOT_TEST_HEADER"]}.to_json
end

delete '/headers' do
    {"headers" => request_headers["HOOT_TEST_HEADER"]}.to_json
end

delete '/wait' do
    sleep 10
    '{"test":"This is a test"}'
end

post '/headers' do
  {"headers" => request_headers["HOOT_TEST_HEADER"], "postdata" => request.body.string}.to_json
end

post '/' do
  {"postdata" => request.body.string}.to_json
end

