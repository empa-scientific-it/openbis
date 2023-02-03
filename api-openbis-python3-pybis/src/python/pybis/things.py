from tabulate import tabulate
from pandas import DataFrame, Series
import pandas as pd


class Things:
    """An object that contains a DataFrame object about an entity  available in openBIS.
    Use .df to work with the DataFrame instead.
    Can be used in a for-loop:

    for sample in openbis.get_samples():
        ...

    You can access an element directly by providing the index number:
        openbis.get_samples()[7]

    Because the order of the elements cannot be ensured, you should choose the identifier instead:
        openbis.get_samples()['/SOME_SPACE/SAMPLE_CODE']

    Of course, if you know the identifier already, you would rather do:
        openbis.get_sample('/SOME_SPACE/SAMPLE_CODE')


    """

    def __init__(
        self,
        openbis_obj,
        entity,
        identifier_name="code",
        additional_identifier=None,
        start_with=None,
        count=None,
        totalCount=None,
        single_item_method=None,
        response=None,
        df_initializer=None,
        objects_initializer=None,
        attrs=None,
        props=None,
    ):
        self.openbis = openbis_obj
        self.entity = entity
        self.__df = None
        self.identifier_name = identifier_name
        self.additional_identifier = additional_identifier
        self.start_with = start_with
        self.count = count
        self.totalCount = totalCount
        self.single_item_method = single_item_method
        self.__objects = None
        self.response = response
        self.__objects_initializer = objects_initializer
        self.__df_initializer = df_initializer
        self.__attrs = attrs
        self.__props = props

    def is_df_initialised(self):
        return self.__df is not None

    def is_objects_initialised(self):
        return self.__objects is not None

    @property
    def df(self):
        if self.__df is None and self.__df_initializer is not None:
            self.__df = self.__df_initializer(
                attrs=self.__attrs, props=self.__props, response=self.response
            )
        return self.__df

    @property
    def objects(self):
        if self.__objects is None and self.__objects_initializer is not None:
            self.__objects = self.__objects_initializer(response=self.response)
        return self.__objects

    def __repr__(self, headers=None, sort_by=None):
        if headers is None:
            headers = list(self.df)
        if sort_by:
            return tabulate(self.df.sort_values(by=sort_by), headers=headers)
        return tabulate(self.df, headers=headers)

    def __len__(self):
        return len(self.df)

    def _repr_html_(self):
        return self.df._repr_html_()

    @staticmethod
    def __create_data_frame(attrs, props, response):
        if len(response) > 0:
            return pd.concat(response)
        else:
            return DataFrame()

    def get_parents(self, **kwargs):
        if self.entity not in ["sample", "dataset"]:
            raise ValueError(f"{self.entity}s do not have parents")

        if self.df is not None and len(self.df) > 0:
            dfs = []
            for ident in self.df[self.identifier_name]:
                # get all objects that have this object as a child == parent
                try:
                    parents = getattr(self.openbis, "get_" + self.entity.lower() + "s")(
                        withChildren=ident, **kwargs
                    )
                    dfs.append(parents.df)
                except ValueError:
                    pass
            return Things(
                self.openbis,
                self.entity,
                self.identifier_name,
                response=dfs,
                df_initializer=self.__create_data_frame,
            )

    def get_children(self, **kwargs):
        if self.entity not in ["sample", "dataset"]:
            raise ValueError(f"{self.entity}s do not have children")

        if self.df is not None and len(self.df) > 0:
            dfs = []
            for ident in self.df[self.identifier_name]:
                # get all objects that have this object as a child == parent
                try:
                    parents = getattr(self.openbis, "get_" + self.entity.lower() + "s")(
                        withParent=ident, **kwargs
                    )
                    dfs.append(parents.df)
                except ValueError:
                    pass

                return Things(
                    self.openbis,
                    self.entity,
                    self.identifier_name,
                    response=dfs,
                    df_initializer=self.__create_data_frame,
                )

    def get_samples(self, **kwargs):
        if self.entity not in ["space", "project", "experiment"]:
            raise ValueError(f"{self.entity}s do not have samples")

        if self.df is not None and len(self.df) > 0:
            dfs = []
            for ident in self.df[self.identifier_name]:
                args = {}
                args[self.entity.lower()] = ident
                try:
                    samples = self.openbis.get_samples(**args, **kwargs)
                    dfs.append(samples.df)
                except ValueError:
                    pass

            return Things(
                self.openbis,
                "sample",
                "identifier",
                response=dfs,
                df_initializer=self.__create_data_frame,
            )

    get_objects = get_samples  # Alias

    def get_datasets(self, **kwargs):
        if self.entity not in ["sample", "experiment"]:
            raise ValueError(f"{self.entity}s do not have datasets")

        if self.df is not None and len(self.df) > 0:
            dfs = []
            for ident in self.df[self.identifier_name]:
                args = {}
                args[self.entity.lower()] = ident
                try:
                    datasets = self.openbis.get_datasets(**args, **kwargs)
                    dfs.append(datasets.df)
                except ValueError:
                    pass

            return Things(
                self.openbis,
                "dataset",
                "permId",
                response=dfs,
                df_initializer=self.__create_data_frame,
            )

    def __getitem__(self, key):
        """elegant way to fetch a certain element from the displayed list.
        If an integer value is given, we choose the row.
        If the key is a list, we return the desired columns (normal dataframe behaviour)
        If the key is a non-integer value, we treat it as a primary-key lookup
        """
        if self.df is not None and len(self.df) > 0:
            row = None
            if isinstance(key, int):
                if self.objects:
                    return self.objects[key]
                else:
                    # get thing by rowid
                    row = self.df.loc[[key]]
            elif isinstance(key, list):
                # treat it as a normal dataframe
                return self.df[key]
            else:
                # get thing by code
                row = self.df[self.df[self.identifier_name] == key.upper()]

            if row is not None:
                # invoke the openbis.get_<entity>() method
                if self.single_item_method:
                    get_item = self.single_item_method
                else:
                    get_item = getattr(self.openbis, "get_" + self.entity)
                if self.additional_identifier is None:
                    return get_item(row[self.identifier_name].values[0])
                ## get an entry using two keys
                else:
                    return get_item(
                        row[self.identifier_name].values[0],
                        row[self.additional_identifier].values[0],
                    )

    def __iter__(self):
        if self.objects:
            for obj in self.objects:
                yield obj
        else:
            if self.single_item_method:
                get_item = self.single_item_method
            else:
                get_item = getattr(self.openbis, "get_" + self.entity)
            for item in self.df[[self.identifier_name]][
                self.identifier_name
            ].iteritems():
                yield get_item(item[1])
                # yield getattr(self.openbis, 'get_' + self.entity)(item[1])

                # return self.df[[self.identifier_name]].to_dict()[self.identifier_name]
