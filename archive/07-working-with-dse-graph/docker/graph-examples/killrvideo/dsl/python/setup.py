from setuptools import setup

setup(
    name='killrvideo_dsl',
    version='0.1.0',
    packages=['killrvideo_dsl'],
    url='https://killrvideo.github.io/',
    description='KillrVideo DSL',
    install_requires=[
        'aenum',
        'dse-graph',
        'gremlinpython'
    ],
    classifiers=[
        "Intended Audience :: Developers",
        "Natural Language :: English",
        "Programming Language :: Python :: 2.7",
        "Programming Language :: Python :: 3.4",
        "Programming Language :: Python :: 3.5",
    ]
)
