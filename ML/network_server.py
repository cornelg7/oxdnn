import zerorpc

class HelloRPC(object):
    def evaluate(self, picture):
        return "Possibly accessible, according to the python NN."

s = zerorpc.Server(HelloRPC())
s.bind("tcp://0.0.0.0:44224")
s.run()