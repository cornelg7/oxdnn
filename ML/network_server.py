import zerorpc

class HelloRPC(object):
    def evaluate(self, filename):   # a png/jpg file will be in ML/temp/filename
        print(filename)             # this file will be resent to the client
        return "Some message for the server"

s = zerorpc.Server(HelloRPC())
s.bind("tcp://0.0.0.0:44224")
s.run()