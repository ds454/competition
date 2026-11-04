#! /bin/sh

# Pull the latest submodule updates
echo Refreshing local-dev...
git submodule update --remote --merge
echo Done!

# Update Maven 
echo Pulling maven dependancies...
mvn dependency:resolve
echo Done!

# Compile protoc files
echo Compiling protoc to Java...
mvn protobuf:generate
echo Done!

# Add apply alias for ptorobuf generation
echo Setting up protobuf compilation command...
echo alias apply=\'/workspaces/competition/local-dev/apply-proto-java.sh\' >> ~/.bashrc
echo Done!

echo Development environment configured.
echo Happy coding!